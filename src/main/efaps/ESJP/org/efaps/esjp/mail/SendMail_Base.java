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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIMail;
import org.efaps.esjp.ci.CITableMail;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("90e9fd8d-2396-4719-b691-7404872dc6bb")
@EFapsRevision("$Rev$")
public abstract class SendMail_Base
    extends AbstractSendMail
{

    /**
     * @param _parameter
     * @return
     * @throws EFapsException
     */
    public Return sendObjectMail(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getInstance();
        if (instance.isValid()) {
            final String templateKey = getTemplateKey(_parameter);
            if (templateKey == null) {
                AbstractSendMail_Base.LOG.error("No property 'Template' defined for Sending Object Mail.");
            } else {
                final QueryBuilder queryBldr = new QueryBuilder(CIMail.TemplateObject);
                queryBldr.addWhereAttrEqValue(CIMail.TemplateObject.Name, templateKey);

                final MultiPrintQuery print = getPrint(_parameter, queryBldr);
                print.addAttribute(CIMail.TemplateObject.IsHtml, CIMail.TemplateObject.Template,
                                CIMail.TemplateObject.Server,  CIMail.TemplateObject.Subject);
                print.executeWithoutAccessCheck();
                String template = null;
                boolean isHtml = true;
                String server = null;
                String subject = null;
                if (print.next()) {
                    template = print.<String>getAttribute(CIMail.TemplateObject.Template);
                    subject = print.<String>getAttribute(CIMail.TemplateObject.Subject);
                    server = print.<String>getAttribute(CIMail.TemplateObject.Server);
                    isHtml  = print.<Boolean>getAttribute(CIMail.TemplateObject.IsHtml);
                }

                if (template == null || (template != null && template.isEmpty())) {
                    AbstractSendMail_Base.LOG.error(
                                    "No valid Template for template '{}' during Sending Object Mail found.",
                                    templateKey);
                } else if (server == null || (server != null && server.isEmpty())) {
                    AbstractSendMail_Base.LOG.error(
                                    "No valid Server for template '{}' during Sending Object Mail found.",
                                    templateKey);
                } else {
                    if (isHtml) {
                        sendHtml(_parameter, server, getObjectString(_parameter, instance, subject),
                                        getObjectString(_parameter, instance, template));
                    } else {
                        sendPlain(_parameter, server, getObjectString(_parameter, instance, subject),
                                        getObjectString(_parameter, instance, template));
                    }
                }
            }
        }
        return new Return();
    }

    protected String getTemplateKey(final Parameter _parameter)
        throws EFapsException
    {
        return getProperty(_parameter, "Template");
    }

    protected MultiPrintQuery getPrint(final Parameter _parameter,
                                       final QueryBuilder _queryBldr)
        throws EFapsException
    {
        return _queryBldr.getPrint();
    }

    /**
     * @param _parameter    Parameter as passed by the efasp API
     * @param _instance     instance the print is based on
     * @param _template     templat eto parse
     * @return String
     * @throws EFapsException on error
     */
    protected String getObjectString(final Parameter _parameter,
                                     final Instance _instance,
                                     final String _string)
        throws EFapsException
    {
        String ret = _string;
        try {
            final ValueParser parser = new ValueParser(new StringReader(_string));
            final ValueList valList = parser.ExpressionString();
            if (valList.getExpressions().size() > 0) {
                final PrintQuery print = new PrintQuery(_instance);
                valList.makeSelect(print);
                print.executeWithoutAccessCheck();
                ret = valList.makeString(_instance, print, TargetMode.VIEW);
            }
        } catch (final ParseException e) {
            throw new EFapsException("Catched Parser Exception.", e);
        }
        return ret;
    }

    /**
     * @param _parameter    Parameter as passed by the efasp API
     * @param _server       Server to be used
     * @param _subject      Subject for the mail
     * @param _htmlContent  content
     * @throws EFapsException on error
     */
    protected void sendHtml(final Parameter _parameter,
                            final String _server,
                            final String _subject,
                            final String _htmlContent)
        throws EFapsException
    {
        try {
            final HtmlEmail email = new HtmlEmail();
            email.setSubject(_subject);
            email.setHtmlMsg(_htmlContent);
            attach(_parameter, email);
            send(_parameter, _server, email);
        } catch (final EmailException e) {
            AbstractSendMail_Base.LOG.error("Could not send Mail.", e);
        }
    }

    /**
     * @param _parameter    Parameter as passed by the efasp API
     * @param _server       Server to be used
     * @param _subject      Subject for the mail
     * @param _plainContent  content
     * @throws EFapsException on error
     */
    protected void sendPlain(final Parameter _parameter,
                            final String _server,
                            final String _subject,
                            final String _plainContent)
        throws EFapsException
    {
        try {
            final SimpleEmail email = new SimpleEmail();
            email.setSubject(_subject);
            email.setMsg(_plainContent);
            send(_parameter, _server, email);
        } catch (final EmailException e) {
            AbstractSendMail_Base.LOG.error("Could not send Mail.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addTo(final Parameter _parameter,
                         final Email _email)
        throws EmailException
    {
        final String[] tos = _parameter.getParameterValues(CITableMail.Mail_SendObjectMailToTable.to.name);
        final String[] toNames = _parameter.getParameterValues(CITableMail.Mail_SendObjectMailToTable.toName.name);
        if (tos != null) {
            for (int i = 0; i< tos.length; i++) {
                final String to = tos[i];
                final String toName = toNames[i];
                if (toName != null && toName.isEmpty()) {
                    _email.addTo(to, toName);
                } else {
                    _email.addTo(to);
                }
            }
        }
    }

    /**
     * @param _parameter Parameter as passed by the efasp API
     * @param _email     mail to attach to
     * @throws EFapsException, EmailException on error
     */
    protected void attach(final Parameter _parameter,
                          final HtmlEmail _email)
        throws EFapsException, EmailException
    {
        if ("true".equalsIgnoreCase(getProperty(_parameter, "Attachment"))) {
            try {
                final Checkout checkout = new Checkout(_parameter.getInstance());
                final InputStream in = checkout.execute();
                final InputStream is = new BufferedInputStream(in);
                final DataSource source = new ByteArrayDataSource(is, "application/pdf");
                final String fileName = checkout.getFileName();
                _email.attach(source, fileName, "-");
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
