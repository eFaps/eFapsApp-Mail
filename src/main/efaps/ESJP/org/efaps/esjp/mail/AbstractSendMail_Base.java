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

import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.mail.utils.Mail;
import org.efaps.esjp.mail.utils.MailSettings;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("4a2ed8b6-3d80-4263-8b81-3fb850577e74")
@EFapsRevision("$Rev$")
public abstract class AbstractSendMail_Base
{
    /**
     * @param _parameter parameter as passed by the eFaps API
     * @param _server server key
     * @return new Session
     * @throws EFapsException on error
     */
    protected Session getSession(final Parameter _parameter,
                                 final String _server) throws EFapsException
    {
        final Properties props = Mail.getSysConfig().getAttributeValueAsProperties(MailSettings.SERVER + _server);
        return  Session.getInstance(props);
    }
}
