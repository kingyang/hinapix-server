package org.openempi.ics.pids;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openempi.data.Person;
import org.openempi.data.PersonReview;
import org.openempi.ics.utility.JdbcHelper;

/**
 * Implements the Correlation Error queue class.  This is used to create,
 * retreive and deltee entries in the correlation error queue from the
 * database.  This class performs all persistence related activities for
 * the Review Queue.
 *
 * This class relies on the caller be able to supply and manipulate
 * PersonReview objects.  The PersonReview class encapsulates a single
 * review request from a user or the system.
 */

public class ReviewQueue
{
    private Logger log = Logger.getLogger("ICS");
    
    /**
     * Persists a PersonReview in the database.  If successful, the
     * personReview unique Id will be filled in as well as the creation date.
     *
     * @param personReview The PersonReview object to submit
     */
    public void submit(PersonReview personReview)
    throws PersonIdServiceException
    {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        int[] persons = personReview.getPersons();
        int id;
        
        if (personReview.getDescr() == null)
        {
            throw new PersonIdServiceException("The description is required for PersonReview");
        }
        
        if (persons == null || persons.length == 0)
        {
            throw new PersonIdServiceException("No Persons specified for the PersonReview");
        }
        
        personReview.setCreateDate(new java.util.Date());
        
        try
        {
            conn = JdbcHelper.getConnection();
            conn.setAutoCommit(false);
            
            st = conn.prepareStatement("select person_queue_seq.nextval from dual");
            rs = st.executeQuery();
            if (rs.next())
            {
                personReview.setId(rs.getInt(1));
            }
            else
            {
                throw new PersonIdServiceException("Unable to get unique id for Person Queue!");
            }
            st.close(); st = null;
            
            log.debug("Inserting PersonReview id: " + personReview.getId());
            
            st = conn.prepareStatement("insert into person_queue (queue_id, description, user_id, user_domain, create_date) values (?, ?, ?, ?, ?)");
            st.setInt(1, personReview.getId());
            st.setString(2, personReview.getDescr());
            if (personReview.getUserId() == null)
                st.setNull(3, java.sql.Types.VARCHAR);
            else
                st.setString(3, personReview.getUserId());
            if (personReview.getDomainId() == null)
                st.setNull(4, java.sql.Types.VARCHAR);
            else
                st.setString(4, personReview.getDomainId());
            st.setTimestamp(5, new Timestamp(personReview.getCreateDate().getTime()));
            
            if (st.executeUpdate() == 0)
            {
                throw new PersonIdServiceException("Unable to insert new PersonReview into the database");
            }
            st.close(); st = null;
            
            for(int i = 0; i < persons.length; i++)
            {
                log.debug("--> Person: " + persons[i]);
                st = conn.prepareStatement("insert into person_queue_element (queue_id, person_id) values (?, ?)");
                st.setInt(1, personReview.getId());
                
                if (persons[i] == 0)
                    throw new PersonIdServiceException("Invalid Person passed to ReviewQueue.submit().  No id known for this person!");
                st.setInt(2, persons[i]);
                if (st.executeUpdate() == 0)
                {
                    throw new PersonIdServiceException("Unable to insert new PersonReview into the database");
                }
                st.close(); st = null;
            }
            
            conn.commit();
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error(e,e);
            try
            {
                if ( conn != null ) conn.rollback();
            }
            catch ( SQLException se2 )
            { }
        }
        finally
        {
            try
            {
                if ( rs != null ) rs.close();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( st != null ) st.close();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( conn != null ) conn.close();
            }
            catch ( SQLException se2 )
            { }
        }
        
    }
    
    
    /**
     * Deletes a Person Review from the database.
     *
     * @param id The unique id of the person review to delete
     */
    public void delete(int id)
    throws PersonIdServiceException
    {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        
        try
        {
            log.debug("Deleting PersonReview id: " + id);
            conn = JdbcHelper.getConnection();
            conn.setAutoCommit(false);
            
            st = conn.prepareStatement("delete from person_queue_element where queue_id = ?");
            st.setInt(1, id);
            if (st.executeUpdate() == 0)
            {
                throw new PersonIdServiceException("Unable to insert new PersonReview into the database");
            }
            st.close(); st = null;
            
            st = conn.prepareStatement("delete from person_queue where queue_id = ?");
            st.setInt(1, id);
            if (st.executeUpdate() == 0)
            {
                throw new PersonIdServiceException("Unable to insert new PersonReview into the database");
            }
            st.close(); st = null;
            
            conn.commit();
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error(e,e);
            try
            {
                if ( conn != null ) conn.rollback();
            }
            catch ( SQLException se2 )
            { }
        }
        finally
        {
            try
            {
                if ( rs != null ) rs.close();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( st != null ) st.close();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( conn != null ) conn.close();
            }
            catch ( SQLException se2 )
            { }
        }
        
    }
    
    /**
     * Retrieves a List of PersonReviews from the database for the given
     * domain.  If domain is null, reviews with no domain specified
     * (system reviews) are returned.
     *
     * @param domainId The domain to retrieve reviews for.  If this string
     * is empty (zero length string) then system reviews
     * are retrieved.  If domainId is null, all reviews
     * are returned.
     * @return List of PersonReview objects.  If none, an empty list is returned.
     */
    public List get(String domainId)
    throws PersonIdServiceException
    {
        ArrayList ret = new ArrayList();
        Connection conn = null;
        PreparedStatement st = null;
        PersonReview personReview = null;
        ResultSet rs = null;
        int lastId = 0;
        int id;
        
        try
        {
            conn = JdbcHelper.getConnection();
            conn.setAutoCommit(false);
            
            if (domainId == null)
            {
                log.debug("Retrieving all PersonReviews");
                st = conn.prepareStatement("select person_queue.queue_id, description, user_id, user_domain, create_date, person_id from person_queue, person_queue_element where person_queue.queue_id = person_queue_element.queue_id order by (person_queue.queue_id)");
            }
            else if (domainId.length() == 0)
            {
                log.debug("Retrieving PersonReviews for system");
                st = conn.prepareStatement("select person_queue.queue_id, description, user_id, user_domain, create_date, person_id from person_queue, person_queue_element where person_queue.queue_id = person_queue_element.queue_id and person_queue.user_domain is null order by (person_queue.queue_id)");
            }
            else
            {
                log.debug("Retrieving PersonReviews for domain: " + domainId);
                st = conn.prepareStatement("select person_queue.queue_id, description, user_id, user_domain, create_date, person_id from person_queue, person_queue_element where person_queue.queue_id = person_queue_element.queue_id and person_queue.user_domain = ? order by (person_queue.queue_id)");
                st.setString(1, domainId);
            }
            rs = st.executeQuery();
            while (rs.next())
            {
                id = rs.getInt(1);
                if (id != lastId)
                {
                    lastId = id;
                    log.debug("Found PersonReview id: " + id);
                    personReview = new PersonReview();
                    personReview.setId(id);
                    personReview.setDescr(rs.getString(2));
                    personReview.setUserId(rs.getString(3));
                    personReview.setDomainId(rs.getString(4));
                    personReview.setCreateDate(new java.util.Date(rs.getTimestamp(5).getTime()));
                    ret.add(personReview);
                }
                int pid = rs.getInt(6);
                log.debug("--> Person: " + pid);
                personReview.addPerson(pid);
            }
            st.close(); st = null;
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error(e,e);
            try
            {
                if ( conn != null ) conn.rollback();
            }
            catch ( SQLException se2 )
            { }
        }
        finally
        {
            try
            {
                if ( rs != null ) rs.close();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( st != null ) st.close();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( conn != null ) conn.close();
            }
            catch ( SQLException se2 )
            { }
        }
        
        log.debug("Retrieved " + ret.size() + " PersonReviews");
        return ret;
    }
    
    /**
     * Checks if reviews are pending.
     *
     * @param domainId The domain to retrieve reviews for all null for system
     * reviews.
     * @return true if reviews are pending, false otherwise.
     */
    public boolean pending(String domainId)
    throws PersonIdServiceException
    {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean ret = false;
        
        try
        {
            conn = JdbcHelper.getConnection();
            conn.setAutoCommit(false);
            
            if (domainId == null)
            {
                log.debug("Checking for pending reviews for null domain");
                st = conn.prepareStatement("select count(*) from person_queue where user_domain is null");
            }
            else
            {
                log.debug("Checking for pending reviews for domain: " + domainId);
                st = conn.prepareStatement("select count(*) from person_queue where user_domain = ?");
                st.setString(1, domainId);
            }
            rs = st.executeQuery();
            int cnt = rs.getInt(1);
            ret = (cnt > 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error(e,e);
        }
        finally
        {
            try
            {
                if ( rs != null ) rs.close();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( st != null ) st.close();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( conn != null ) conn.close();
            }
            catch ( SQLException se2 )
            { }
        }
        
        log.debug("Pending: " + ret);
        return ret;
    }
    
    public boolean exists(String description, Person p) throws PersonIdServiceException
    {
        int personId = p.getOidInt();
        log.debug("Looking to see if review queue entry already exists for: \n" +
                "Description: "+description+"\n" +
                "Person (id): "+personId);
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean ret = false;
        
        try
        {
            conn = JdbcHelper.getConnection();
            conn.setAutoCommit(false);
            
            if(description != null && p != null)
            {
                String reviewCheckSql = "select count(PQ.queue_id) from person_queue PQ, person_queue_element PQE " +
                        "where PQ.description = ? and " +
                        "PQ.queue_id = PQE.queue_id and " +
                        "PQE.person_id = ?";
                log.debug("reviewCheckSql: "+reviewCheckSql);
                st = conn.prepareStatement(reviewCheckSql);
                st.setString(1, description);
                st.setInt(2, personId);
                rs = st.executeQuery();
                rs.next();
                int cnt = rs.getInt(1);
                log.debug(cnt +" queue entries exist");
                ret = (cnt > 0);
            }
            return ret;
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            throw new PersonIdServiceException("Unable to determine if Review Queue entry already exists.");
        }
        finally
        {
            try
            {
                if ( rs != null ) rs.close();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( st != null ) st.close();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( conn != null ) conn.close();
            }
            catch ( SQLException se2 )
            { }
        }
    }
    
}
