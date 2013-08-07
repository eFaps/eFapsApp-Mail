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

import java.io.StringReader;
import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.esjp.ci.CIMail;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("90e9fd8d-2396-4719-b691-7404872dc6bb")
@EFapsRevision("$Rev$")
public abstract class SendMail_Base
{

    protected static final Logger LOG = LoggerFactory.getLogger(SendMail.class);

    public Return sendObjectMail(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getInstance();
        if (instance.isValid()) {
            final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
            final String templateKey = (String) properties.get("template");
            if (templateKey == null) {
                SendMail_Base.LOG.error("No property 'template' defined for Sending Object Mail.");
            } else {
                final QueryBuilder queryBldr = new QueryBuilder(CIMail.TemplateObject);
                queryBldr.addWhereAttrEqValue(CIMail.TemplateObject.Name, templateKey);
                final MultiPrintQuery print = queryBldr.getPrint();
                print.addAttribute(CIMail.TemplateObject.IsHtml, CIMail.TemplateObject.Template);
                print.executeWithoutAccessCheck();
                String template = null;
                if (print.next()) {
                    template = print.<String>getAttribute(CIMail.TemplateObject.Template);
                }

                if (template == null || (template != null && template.isEmpty())) {
                    SendMail_Base.LOG.error("No valid Template for template '' during Sending Object Mail found.",
                                    templateKey);
                } else {

                }
            }
        }
        return new Return();
    }


    protected String getObjectString(final Parameter _parameter,
                                     final Instance _instance,
                                     final String _template)
        throws EFapsException
    {
        String ret = "";
        try {
            final ValueParser parser = new ValueParser(new StringReader(_template));
            final ValueList list = parser.ExpressionString();
            if (list.getExpressions().size() > 0) {
                final PrintQuery print = new PrintQuery(_instance);
                final ValueList valList = new ValueList();
                valList.makeSelect(print);
                print.executeWithoutAccessCheck();
                ret = valList.makeString(_instance, print, TargetMode.VIEW);
            }
        } catch (final ParseException e) {
            throw new EFapsException("Catched Parser Exception.", e);
        }
        return ret;
    }
}
