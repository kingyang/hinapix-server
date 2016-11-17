
DELETE FROM person WHERE person_id IN
(SELECT dh.person_id FROM document_header dh INNER JOIN person_info pi USING(dh_id));