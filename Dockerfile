# Pull base image
FROM dev-docker-registry.kapa.ware.fi/e-identification-tomcat-apache2-shibd-sp-base-image
COPY target/site /site

COPY conf /tmp/conf
COPY target/kapa-service-provider.war /opt/service-provider/
WORKDIR /opt/service-provider

RUN mkdir -p /etc/shibboleth/idp-metadata /data00/templates/store /usr/share/tomcat/conf/Catalina/localhost/
RUN jar -xvf kapa-service-provider.war
RUN cp /tmp/conf/shibboleth/attribute-map.xml /etc/shibboleth/attribute-map.xml  && \
    cp /tmp/conf/shibboleth/bindingTemplateEN.html /etc/shibboleth/bindingTemplateEN.html && \
    cp /tmp/conf/shibboleth/bindingTemplateSV.html /etc/shibboleth/bindingTemplateSV.html && \
    cp /tmp/conf/shibboleth/bindingTemplateCX.html /etc/shibboleth/bindingTemplateCX.html && \
    cp /tmp/conf/shibboleth/bindingTemplateFICountry.html /etc/shibboleth/bindingTemplateFICountry.html && \
    cp /tmp/conf/shibboleth/security-policy.xml /etc/shibboleth/security-policy.xml && \
    cp /tmp/conf/tomcat/sp.xml /usr/share/tomcat/conf/Catalina/localhost/ && \
    cp /tmp/conf/tomcat/server.xml /usr/share/tomcat/conf/  && \
    cp /tmp/conf/tomcat/logging.properties /usr/share/tomcat/conf/logging.properties && \
    cp /tmp/conf/apache/envvars /etc/apache2/envvars && \
    cp /tmp/conf/shibboleth/shibd.logger /etc/shibboleth/shibd.logger && \
:                             && \
: Templates                   && \
:                             && \
    cp /tmp/conf/shibboleth/shibboleth2.xml.template.local /data00/templates/store/   && \
    cp /tmp/conf/shibboleth/shibboleth2.xml.template.dev /data00/templates/store/   && \
    cp /tmp/conf/shibboleth/shibboleth2.xml.template.kete /data00/templates/store/   && \
    cp /tmp/conf/shibboleth/shibboleth2.xml.template.test /data00/templates/store/  && \
    cp /tmp/conf/shibboleth/shibboleth2.xml.template.prod /data00/templates/store/  && \
    cp /tmp/conf/apache/default-ssl.conf.template /data00/templates/store/  && \
    cp /tmp/conf/tomcat/service-provider.properties.template /data00/templates/store/  && \
    cp /tmp/conf/shibboleth/sp-setenv.sh.template /data00/templates/store/  && \
    cp /tmp/conf/logging/log4j.properties.template /data00/templates/store/  && \
:                             && \
:                             && \
:                             && \
    cp -r /tmp/conf/ansible /data00/templates/store/ansible   && \
    chown -R tomcat:tomcat /opt/service-provider && \
    chown -R tomcat:tomcat /usr/share/tomcat && \
    chown -R _shibd:_shibd /etc/shibboleth/idp-metadata/ && \
    rm -fr /usr/share/tomcat/webapps/* && \
    rm -fr /usr/share/tomcat/server/webapps/* && \
    rm -fr /usr/share/tomcat/conf/Catalina/localhost/host-manager.xml && \
    rm -fr /usr/share/tomcat/conf/Catalina/localhost/manager.xml && \
    ln -sf /etc/apache2/sites-available/default-ssl.conf /etc/apache2/sites-enabled/default-ssl.conf && \
    ln -sf /data00/deploy/default-ssl.conf /etc/apache2/sites-available/default-ssl.conf && \
    mkdir -p /opt/service-provider-properties/ && \
    mkdir -p /usr/share/tomcat/properties && \
    ln -sf /data00/deploy/service-provider.properties /opt/service-provider-properties/service-provider.properties && \
    ln -sf /data00/deploy/shibboleth2.xml /etc/shibboleth/shibboleth2.xml && \
    ln -sf /data00/deploy/sp-setenv.sh /usr/share/tomcat/bin/setenv.sh && \
    ln -sf /data00/deploy/kapa-ca /opt/kapa-ca && \
    ln -sf /data00/deploy/tomcat_keystore /usr/share/tomcat/properties/tomcat_keystore

CMD \
    mkdir -p /data00/logs && \
    chown -R tomcat:tomcat /data00/deploy && \
    chmod -R 777 /data00/logs && \
    ln -sf /data00/deploy/certs/* /etc/ssl/certs/ && \
    ln -sf /data00/deploy/private/* /etc/ssl/private/ && \
    ln -sf /data00/deploy/sp_metadata/* /etc/shibboleth/ && \
    service apache2 restart && service shibd restart && \
    sudo -u tomcat sh -c '/usr/share/tomcat/bin/catalina.sh run'
