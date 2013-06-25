#!/bin/sh

# create 4 private keys and certificate requests for client,server,ca,badbad
for i in ca client server badbad ;do
	openssl genrsa -out ${i}_privateKey_rsa_nopwd_traditional.pem 1024
	openssl req -new -key ${i}_privateKey_rsa_nopwd_traditional.pem -subj "/C=AU/ST=Western/O=Example LTD/OU=ExampleDep/CN=${i}/emailAddress=${i}@${i}.com" -out ${i}_certificateRequest.pem
	openssl x509 -req -days 730 -in ${i}_certificateRequest.pem -signkey ${i}_privateKey_rsa_nopwd_traditional.pem -out ${i}_certificate_selfSigned.pem
	openssl rsa -inform PEM -outform DER -in ${i}_privateKey_rsa_nopwd_traditional.pem -out ${i}_privateKey_rsa_nopwd_traditional.der 
done

# create certificates for client,server,badbad signed by ca. Additionally one certificate for client signed by badbad
[ -d demoCA ] || mkdir demoCA
[ -d demoCA/newcerts ] || mkdir demoCA/newcerts
[ -f demoCA/index.txt ] || touch demoCA/index.txt
[ -f demoCA/serial ] || echo '00' > demoCA/serial
for i in client server badbad ;do
	openssl ca -batch -cert ca_certificate_selfSigned.pem -keyfile ca_privateKey_rsa_nopwd_traditional.pem -days 730 -in ${i}_certificateRequest.pem -out ${i}_certificate_signedByCA.pem
done
rm demoCA/index.txt
touch demoCA/index.txt
echo '00' > demoCA/serial
openssl ca -batch -cert badbad_certificate_selfSigned.pem -keyfile badbad_privateKey_rsa_nopwd_traditional.pem -days 730 -in client_certificateRequest.pem -out client_certificate_signedByBadbad.pem

# create different representations of the clients private key
openssl rsa -inform PEM -outform PEM -in client_privateKey_rsa_nopwd_traditional.pem -out client_privateKey_rsa_pwdclient_traditional.pem -passout pass:client -des3 
openssl rsa -inform PEM -outform DER -in client_privateKey_rsa_nopwd_traditional.pem -out client_privateKey_rsa_pwdclient_traditional.der -passout pass:client -des3 
openssl pkcs8 -topk8 -inform PEM -outform PEM -in client_privateKey_rsa_nopwd_traditional.pem -out client_privateKey_rsa_nopwd_pkcs8.pem -nocrypt
openssl pkcs8 -topk8 -inform PEM -outform DER -in client_privateKey_rsa_nopwd_traditional.pem -out client_privateKey_rsa_nopwd_pkcs8.der -nocrypt
openssl pkcs8 -topk8 -inform PEM -outform PEM -in client_privateKey_rsa_nopwd_traditional.pem -out client_privateKey_rsa_pwdclient_pkcs8.pem -passout pass:client
openssl pkcs8 -topk8 -inform PEM -outform DER -in client_privateKey_rsa_nopwd_traditional.pem -out client_privateKey_rsa_pwdclient_pkcs8.der -passout pass:client

# create PKCS12 and java keystore files for client,server,badbad
for i in client server badbad ;do
	openssl pkcs12 -export -chain -out ${i}_certificateAndKey_signedByCA_pwd${i}.p12 -in ${i}_certificate_signedByCA.pem -inkey ${i}_privateKey_rsa_nopwd_traditional.pem -CAfile ca_certificate_selfSigned.pem -passout pass:${i}
	keytool -importkeystore -destkeystore ${i}_certificateAndKey_signedByCA_pwd${i}.jks -deststorepass ${i} -destkeypass ${i} -srckeystore ${i}_certificateAndKey_signedByCA_pwd${i}.p12 -srcstorepass ${i} -srcstoretype PKCS12 -srcalias 1 -destalias ${i}
done
