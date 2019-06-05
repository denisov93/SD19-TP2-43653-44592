cp base-truststore.ks client-truststore.ks
echo "Use password: changeit"
keytool -importcert -file profileserver.cert -alias profilestore -keystore client-truststore.ks
keytool -importcert -file postserver.cert -alias poststore -keystore client-truststore.ks
keytool -importcert -file mediaserver.cert -alias mediastore -keystore client-truststore.ks
keytool -importcert -file microgram.cert -alias microgram -keystore client-truststore.ks
