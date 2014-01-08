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

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIMail;
import org.efaps.esjp.erp.eventdefinition.AbstractEventDefinition;
import org.efaps.util.EFapsException;
import org.quartz.JobExecutionContext;


@EFapsUUID("6b5617f3-8667-4c57-9c59-54fe92c6c270")
@EFapsRevision("$Rev$")
public abstract class MailEventDefinition_Base
    extends AbstractEventDefinition
{
    @Override
    public void execute(final Instance _defInstance,
                        final JobExecutionContext _jobExec)
        throws EFapsException
    {
        init(_defInstance, _jobExec);

        for (final EventSchedule event : getEvents()) {

            final SendMail sender = new SendMail() {
                @Override
                protected String getTemplateKey(final Parameter _parameter)
                {
                    return MailEventDefinition_Base.this.getTemplateKey();
                }

                @Override
                protected MultiPrintQuery getPrint(final Parameter _parameter,
                                                   final QueryBuilder _queryBldr)
                    throws EFapsException
                {
                    _queryBldr.addWhereAttrEqValue(CIMail.TemplateAbstract.Company, event.getCompany().getId());
                    final InstanceQuery query = _queryBldr.getQuery();
                    query.setCompanyDepended(false);
                    return new MultiPrintQuery(query.executeWithoutAccessCheck());
                }

                @Override
                protected void addTo(final Parameter _parameter,
                                     final Email _email)
                    throws EmailException
                {
                    MailEventDefinition_Base.this.addTo( _email);
                }
            };

            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.INSTANCE, event.getInstance());
            sender.sendObjectMail(parameter);
        }
    }

    /**
     * @param _email
     * @return
     */
    protected void addTo(final Email _email)
    {
        for (int i = 1; i < 100; i++) {
            final String mailto = getProperties().getProperty("MailTo" + String.format("%02d", i));
            if (mailto != null) {
                try {
                    _email.addTo(mailto);
                } catch (final EmailException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
    }

    protected String getTemplateKey()
    {
        return getProperties().getProperty("MailTemplate");
    }
}
