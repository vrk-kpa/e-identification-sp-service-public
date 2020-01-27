# Pull base image
FROM e-identification-docker-virtual.vrk-artifactory-01.eden.csc.fi/e-identification-base-centos7-shibd_v3
COPY target/site /site

COPY conf /tmp/conf
COPY target/kapa-service-provider.war /opt/service-provider/
WORKDIR /opt/service-provider

RUN mkdir -p /etc/shibboleth/idp-metadata \ 
             /data00/templates/store \
             /opt/tomcat/conf/Catalina/localhost/ 

RUN jar -xvf kapa-service-provider.war
RUN cp /tmp/conf/shibboleth/attribute-map.xml /etc/shibboleth/attribute-map.xml  && \
    cp /tmp/conf/shibboleth/bindingTemplateEN.html /etc/shibboleth/bindingTemplateEN.html && \
    cp /tmp/conf/shibboleth/bindingTemplateSVT.html /etc/shibboleth/bindingTemplateSVT.html && \
    cp /tmp/conf/shibboleth/bindingTemplateFIT.html /etc/shibboleth/bindingTemplateFIT.html && \
    cp /tmp/conf/shibboleth/bindingTemplateENT.html /etc/shibboleth/bindingTemplateENT.html && \
    cp /tmp/conf/shibboleth/bindingTemplateSV.html /etc/shibboleth/bindingTemplateSV.html && \
    cp /tmp/conf/shibboleth/bindingTemplateCX.html /etc/shibboleth/bindingTemplateCX.html && \
    cp /tmp/conf/shibboleth/bindingTemplateFICountry.html /etc/shibboleth/bindingTemplateFICountry.html && \
    cp /tmp/conf/shibboleth/bindingTemplateEidas.html /etc/shibboleth/bindingTemplateEidas.html && \
    cp /tmp/conf/shibboleth/bindingTemplateEidasTestXX.html /etc/shibboleth/bindingTemplateEidasTestXX.html && \
    cp /tmp/conf/shibboleth/bindingTemplateDE.html /etc/shibboleth/bindingTemplateDE.html && \
    cp /tmp/conf/shibboleth/security-policy.xml /etc/shibboleth/security-policy.xml && \
    cp /tmp/conf/tomcat/sp.xml /opt/tomcat/conf/Catalina/localhost/ && \
    cp /tmp/conf/tomcat/server.xml /opt/tomcat/conf/  && \
    cp /tmp/conf/tomcat/logging.properties /opt/tomcat/conf/logging.properties && \
    cp /tmp/conf/shibboleth/shibd.logger /etc/shibboleth/shibd.logger && \
:                             && \
: Templates                   && \
:                             && \
    cp /tmp/conf/shibboleth/shibboleth2.xml.template.local /data00/templates/store/   && \
    cp /tmp/conf/shibboleth/shibboleth2.xml.template.dev /data00/templates/store/   && \
    cp /tmp/conf/shibboleth/shibboleth2.xml.template.kete /data00/templates/store/   && \
    cp /tmp/conf/shibboleth/shibboleth2.xml.template.test /data00/templates/store/  && \
    cp /tmp/conf/shibboleth/shibboleth2.xml.template.prod /data00/templates/store/  && \
    cp /tmp/conf/shibboleth/attribute-policy.xml.template /data00/templates/store/ && \
    cp /tmp/conf/httpd/sp-ssl.conf.template /data00/templates/store/  && \
    cp /tmp/conf/httpd/httpd.conf.template /data00/templates/store && \
    cp /tmp/conf/tomcat/service-provider.properties.template /data00/templates/store/  && \
    cp /tmp/conf/shibboleth/sp-setenv.sh.template /data00/templates/store/  && \
    cp /tmp/conf/logging/log4j.properties.template /data00/templates/store/  && \
:                             && \
:                             && \
:                             && \
    cp -r /tmp/conf/ansible /data00/templates/store/ansible   && \
    chown -R tomcat:tomcat /opt/service-provider && \
    chown -R tomcat:tomcat /opt/tomcat && \
    chown -R shibd:shibd /etc/shibboleth/idp-metadata/ && \
    rm -fr /opt/tomcat/webapps/* && \
    rm -fr /opt/tomcat/server/webapps/* && \
    rm -fr /opt/tomcat/conf/Catalina/localhost/host-manager.xml && \
    rm -fr /opt/tomcat/conf/Catalina/localhost/manager.xml && \
    ln -sf /data00/deploy/sp-ssl.conf /etc/httpd/conf/sp-ssl.conf && \
    ln -sf /data00/deploy/httpd.conf /etc/httpd/conf/httpd.conf && \
    mkdir -p /opt/service-provider-properties/ && \
    mkdir -p /opt/tomcat/properties && \
    ln -sf /data00/deploy/service-provider.properties /opt/service-provider-properties/service-provider.properties && \
    ln -sf /data00/deploy/shibboleth2.xml /etc/shibboleth/shibboleth2.xml && \
    ln -sf /data00/deploy/attribute-policy.xml /etc/shibboleth/attribute-policy.xml && \
    ln -sf /data00/deploy/sp-setenv.sh /opt/tomcat/bin/setenv.sh && \
    ln -sf /data00/deploy/kapa-ca /opt/kapa-ca && \
    ln -sf /data00/deploy/tomcat_keystore /opt/tomcat/properties/tomcat_keystore 

#Need to change user to shibd in the start script    
RUN  sed -i -e 's/SHIBD_USER=root/SHIBD_USER=shibd/' /etc/shibboleth/shibd-redhat

RUN chmod +x /etc/shibboleth/shibd-redhat && \
    echo $'export LD_LIBRARY_PATH=/opt/shibboleth/lib64:$LD_LIBRARY_PATH\n' > /etc/sysconfig/shibd && \
    chmod +x /etc/sysconfig/shibd

CMD \
    rm -rf /run/httpd/httpd.pid && \
    mkdir -p /data00/logs && \
    mkdir -p /etc/ssl/private && \
    chown -R tomcat:tomcat /data00/deploy && \
    chmod -R 777 /data00/logs && \
    ln -sf /data00/deploy/certs/* /etc/ssl/certs/ && \
    ln -sf /data00/deploy/private/* /etc/ssl/private/ && \
    ln -sf /data00/deploy/sp_metadata/* /etc/shibboleth/ && \
    httpd -k restart && \
    /etc/shibboleth/shibd-redhat start && \
    /sbin/runuser tomcat -s /bin/bash -c "/opt/tomcat/bin/catalina.sh run" 


