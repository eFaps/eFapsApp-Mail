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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.ci.CIAdminUser;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.esjp.ci.CIERP;
import org.efaps.esjp.ci.CIMail;
import org.efaps.esjp.ci.CITableMail;
import org.efaps.esjp.erp.CommonDocument;
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

    @Override
    protected void addCc(final Parameter _parameter,
                         final Email _email)
        throws EFapsException, EmailException
    {
        super.addCc(_parameter, _email);
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminUser.Person);
        queryBldr.addWhereAttrEqValue(CIAdminUser.Person.ID, Context.getThreadContext().getPersonId());
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttributeSet(CIAdminUser.Person.EmailSet.name);
        multi.executeWithoutAccessCheck();
        while (multi.next()) {
            final Map<String, Object> mailSet = multi.getAttributeSet(CIAdminUser.Person.EmailSet.name);
            if (mailSet != null && mailSet.containsKey("Email")) {
                @SuppressWarnings("unchecked")
                final ArrayList<String> emails = (ArrayList<String>) mailSet.get("Email");
                for (final String email :  emails) {
                    _email.addCc(email);
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

    @SuppressWarnings("unchecked")
    public Return getJavaScriptUIValue(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        final MailDoc doc = new MailDoc();
        final List<Map<String, String>> values = new ArrayList<Map<String, String>>();

        if (_parameter.getInstance().getType().isKindOf(CIERP.DocumentAbstract.getType())) {
            final PrintQuery print = new PrintQuery(_parameter.getInstance());
            // Contacts_ClassPointOfContact
            final Type pocType = Type.get(UUID.fromString("b4a4a330-3520-45e5-bba6-6ba14d093ff7"));
            if (pocType != null) {
                final SelectBuilder selName = SelectBuilder.get().linkto(CIERP.DocumentAbstract.Contact)
                                .clazz(pocType.getUUID()).attributeset("PointOfContactSet").attribute("Name");
                final SelectBuilder selEmail = SelectBuilder.get().linkto(CIERP.DocumentAbstract.Contact)
                                .clazz(pocType.getUUID()).attributeset("PointOfContactSet").attribute("Email");
                print.addSelect(selName, selEmail);
                if (print.executeWithoutAccessCheck()) {
                    final Object mailObj = print.getSelect(selEmail);
                    final Object nameObj = print.getSelect(selName);
                    if (mailObj != null) {
                        if (mailObj instanceof String) {
                            final Map<String, String> map = new HashMap<String, String>();
                            values.add(map);
                            map.put("toName", nameObj.toString());
                            map.put("to", mailObj.toString());
                        } else if (mailObj instanceof List) {
                            final ArrayList<String> names = (ArrayList<String>) nameObj;
                            final ArrayList<String> emails = (ArrayList<String>) mailObj;
                            final Iterator<String> nameIter = names.iterator();
                            final Iterator<String> emailIter = emails.iterator();
                            while (emailIter.hasNext()) {
                                final Map<String, String> map = new HashMap<String, String>();
                                values.add(map);
                                map.put("toName", nameIter.hasNext() ? nameIter.next() : "");
                                map.put("to", emailIter.next());
                            }
                        }
                    }
                }
            }
        }

        final StringBuilder js = new StringBuilder()
                        .append("<script type=\"text/javascript\">\n");
        js.append(doc.getTableRemoveScript(_parameter, "toTable"))
                        .append(doc.getTableAddNewRowsScript(_parameter, "toTable", values,
                                        null, false, false, null));
        js.append("\n</script>\n");
        ret.put(ReturnValues.SNIPLETT, js.toString());
        return ret;
    }


    public static class MailDoc
        extends CommonDocument
    {

        @Override
        public StringBuilder getTableRemoveScript(final Parameter _parameter,
                                                  final String _tableName)
        {
            return super.getTableRemoveScript(_parameter, _tableName, false, false);
        }

        @Override
        protected StringBuilder getTableAddNewRowsScript(final Parameter _parameter,
                                                         final String _tableName,
                                                         final Collection<Map<String, String>> _values,
                                                         final StringBuilder _onComplete,
                                                         final boolean _onDomReady,
                                                         final boolean _wrapInTags,
                                                         final Set<String> _nonEscapeFields)
        {
            return super.getTableAddNewRowsScript(_parameter, _tableName, _values, _onComplete, _onDomReady,
                            _wrapInTags, _nonEscapeFields);
        }
    }

}
