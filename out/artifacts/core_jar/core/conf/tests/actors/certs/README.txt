README.txt - This file.
keystore.key - initial unsigned generated key, saved and never touched.  In case something goes FUBAR.
EHR_MISYS.key - copy of key, for uniform naming.  Used to generate csr.
EHR_MISYS.csr - signing request.  Sent to CA.
EHR_MISYS.cer - signed cert made by CA from .csr.  Recieved from CA.
EHR_MISYS_KEY.key - Keystore with root certs and signed master key.
Identrus_Test_Root.cer - Root cert.
Wells_CA.cer - Intermediate cert.
EHR_MISYS_TRUST.key - keystore for holding cert that signed all other certs.

EMR_MISYS_08_KEY.p12 - self-signed certificate keystore
EMR_MISYS_08_TRUST.jsk - truststore 

to generate key:
openssl pkcs12 -export -out keystore.pkcs12 -in test_sys_1.cert.pem -inkey test_sys_1.key.pem
to generate truststore:
keytool -import -alias mesa -file mesa.cert -keystore TrustStore


