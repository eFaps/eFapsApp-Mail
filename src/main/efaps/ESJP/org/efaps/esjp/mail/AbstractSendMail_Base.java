/*
 * Copyright 2003 - 2013 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.mail;

import java.util.Properties;

import javax.mail.Session;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.mail.utils.Mail;
import org.efaps.esjp.mail.utils.MailSettings;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: AbstractSendMail_Base.java 9995 2013-08-07 19:34:36Z
 *          jan@moxter.net $
 */
@EFapsUUID("4a2ed8b6-3d80-4263-8b81-3fb850577e74")
@EFapsRevision("$Rev$")
public abstract class AbstractSendMail_Base
{

    /**
     * Logger for this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractSendMail.class);

    /**
     * @param _parameter parameter as passed by the eFaps API
     * @param _server server key
     * @return new Session
     * @throws EFapsException on error
     */
    protected Session getSession(final Parameter _parameter,
                                 final String _server)
        throws EFapsException
    {
        final Properties props = Mail.getSysConfig().getAttributeValueAsProperties(MailSettings.SERVER + _server);
        AbstractSendMail_Base.LOG.debug("Getting Session with Properties: {}", props);
        return Session.getInstance(props);
    }

    /**
     * Send the mail.
     * @param _parameter    Parameter as passed by the eFasp API
     * @param _server       Server to be used
     * @param _email        the email to be send
     * @throws EFapsException   on error
     * @throws EmailException   on email error
     */
    protected void send(final Parameter _parameter,
                        final String _server,
                        final Email _email)
        throws EFapsException, EmailException
    {
        setFrom(_parameter, _email);
        addTo(_parameter, _email);
        _email.setMailSession(getSession(_parameter, _server));
        _email.send();
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _email        the email to be send
     * @throws EFapsException   on error
     * @throws EmailException   on email error
     */
    protected void setFrom(final Parameter _parameter,
                           final Email _email)
        throws EFapsException, EmailException
    {
        // as default: nothing
        if (_email.getFromAddress() == null ) {
            final String mail = Mail.getSysConfig().getAttributeValue(MailSettings.DEFAULTFROM);
            if (mail !=  null) {
                _email.setFrom(mail);
            }
        }
    }

    /**
     * @param _parameter    Parameter as passed by the eFasp API
     * @param _email        the email to be send
     * @throws EFapsException   on error
     * @throws EmailException   on email error
     */
    protected abstract void addTo(final Parameter _parameter,
                                  final Email _email)
        throws EFapsException, EmailException;

}
