keytool -genkey -alias server -keyalg RSA -validity 365 -keystore profileserver.ks -storetype pkcs12
keytool -exportcert -alias server -keystore profileserver.ks -file profileserver.cert
keytool -genkey -alias server -keyalg RSA -validity 365 -keystore postserver.ks -storetype pkcs12
keytool -exportcert -alias server -keystore postserver.ks -file postserver.cert
keytool -genkey -alias server -keyalg RSA -validity 365 -keystore mediaserver.ks -storetype pkcs12
keytool -exportcert -alias server -keystore mediaserver.ks -file mediaserver.cert
keytool -genkey -alias server -keyalg RSA -validity 365 -keystore microgram.ks -storetype pkcs12
keytool -exportcert -alias server -keystore microgram.ks -file microgram.cert

