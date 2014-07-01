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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Session;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Person;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Context;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.mail.utils.Mail;
import org.efaps.esjp.mail.utils.MailSettings;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("4a2ed8b6-3d80-4263-8b81-3fb850577e74")
@EFapsRevision("$Rev$")
public abstract class AbstractSendMail_Base
    extends AbstractCommon
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSendMail.class);

    /**
     * Map for parameters used with a substitutor.
     */
    private final Map<String,String> parameters = new HashMap<String,String>();

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
     * @param _objects      additional parameters
     * @throws EFapsException   on error
     * @throws EmailException   on email error
     */
    protected void send(final Parameter _parameter,
                        final String _server,
                        final Email _email,
                        final Object... _objects)
        throws EFapsException, EmailException
    {
        setFrom(_parameter, _email);
        addTo(_parameter, _email, _objects);
        addBcc(_parameter, _email, _objects);
        addCc(_parameter, _email, _objects);
        addReplyTo(_parameter, _email, _objects);
        _email.setMailSession(getSession(_parameter, _server));
        _email.send();
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _email        the email to be send
     * @param _objects      additional parameters
     * @throws EFapsException   on error
     * @throws EmailException   on email error
     */
    protected void setFrom(final Parameter _parameter,
                           final Email _email,
                           final Object... _objects)
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
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _email        the email to be send
     * @param _objects      additional parameters
     * @throws EFapsException   on error
     * @throws EmailException   on email error
     */
    protected void addCc(final Parameter _parameter,
                         final Email _email,
                         final Object... _objects)
        throws EFapsException, EmailException
    {
        // as default: nothing
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _email        the email to be send
     * @param _objects      additional parameters
     * @throws EFapsException   on error
     * @throws EmailException   on email error
     */
    protected void addBcc(final Parameter _parameter,
                          final Email _email,
                          final Object... _objects)
        throws EFapsException, EmailException
    {
        // as default: nothing
    }

    /**
     * @param _parameter    Parameter as passed by the eFaps API
     * @param _email        the email to be send
     * @param _objects      additional parameters
     * @throws EFapsException   on error
     * @throws EmailException   on email error
     */
    protected void addReplyTo(final Parameter _parameter,
                              final Email _email,
                              final Object... _objects)
        throws EFapsException, EmailException
    {
        // as default: nothing
    }

    /**
     * @param _parameter    Parameter as passed by the eFasp API
     * @param _email        the email to be send
     * @param _objects      additional parameters
     * @throws EFapsException   on error
     * @throws EmailException   on email error
     */
    protected abstract void addTo(final Parameter _parameter,
                                  final Email _email,
                                  final Object... _objects)
        throws EFapsException, EmailException;

    /**
     * Getter method for the instance variable {@link #parameters}.
     *
     * @return value of instance variable {@link #parameters}
     * @throws EFapsException   on error
     */
    public Map<String, String> getParameters()
        throws EFapsException
    {
        if (this.parameters.isEmpty()) {
            final Person person = Context.getThreadContext().getPerson();
            this.parameters.put("user.firstname", person.getFirstName());
            this.parameters.put("user.lastname", person.getLastName());
            this.parameters.put("user.name", person.getName());
            final PrintQuery print = new PrintQuery(CIAdminUser.Person.getType(), Context.getThreadContext().getPersonId());
            print.addAttribute(CIAdminUser.Person.EmailSet);
            print.addAttribute("PhoneSet");
            print.executeWithoutAccessCheck();
            final Map<String, Object> emailSet = print.getAttributeSet("EmailSet");
            if (emailSet != null) {
                @SuppressWarnings("unchecked")
                final List<Boolean> primaryValues = (List<Boolean>) emailSet.get("Primary");
                @SuppressWarnings("unchecked")
                final List<String> emailValues = (List<String>) emailSet.get("Email");
                int i =0;
                for (final Boolean val : primaryValues) {
                    if (val) {
                        this.parameters.put("user.mail", emailValues.get(i));
                        break;
                    } else {
                        i++;
                    }
                }
            }
            final Map<String, Object> phoneSet = print.getAttributeSet("PhoneSet");
            if (phoneSet != null) {
                @SuppressWarnings("unchecked")
                final List<String> phoneValues = (List<String>) phoneSet.get("Phone");
                String phone = "";
                for (final String phoneStr : phoneValues) {
                    if (!phone.isEmpty()) {
                        phone = phone +  ". ";
                    }
                    phone = phone + phoneStr;
                }
                this.parameters.put("user.phone",phone);
            }
        }
        return this.parameters;
    }

    /**
     * Example:<br>
     *<br>
     * getParameters().put("animal", "quick brown fox");<br>
     * getParameters().put("target", "lazy dog");<br>
     * String templateString = "The ${animal} jumped over the ${target}.";<br>
     *<br>
     * yielding:<br>
     * The quick brown fox jumped over the lazy dog.<br>
     *
     * @param _template Template to be substituted
     * @return new String
     * @throws EFapsException   on error
     */
    protected String substitute(final String _template,
                                final boolean _escape4Html)
        throws EFapsException
    {
        Map<String,String> parameters;
        if (_escape4Html) {
            parameters = new HashMap<String,String>();
            for (final Entry<String,String> entry: getParameters().entrySet()) {
                parameters.put(entry.getKey(), StringEscapeUtils.escapeHtml4(entry.getValue()));
            }
        } else {
            parameters = getParameters();
        }
        final StrSubstitutor sub = new StrSubstitutor(parameters);
        return sub.replace(_template);
    }
}
