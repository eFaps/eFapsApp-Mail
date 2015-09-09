/*
 * Copyright 2003 - 2015 The eFaps Team
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
 */
package org.efaps.esjp.mail;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.esjp.ci.CIFormMail;
import org.efaps.esjp.ci.CIMail;
import org.efaps.esjp.common.AbstractCommon;
import org.efaps.esjp.common.parameter.ParameterUtil;
import org.efaps.util.EFapsException;

/**
 * The Class Template_Base.
 *
 * @author The eFaps Team
 */
@EFapsUUID("6d8699b0-3f25-4de4-a52e-88d510cf2455")
@EFapsApplication("eFapsApp-Mail")
public abstract class Template_Base
    extends AbstractCommon
{

    /**
     * Send test mail.
     *
     * @param _parameter the _parameter
     * @return the return
     * @throws EFapsException
     */
    public Return sendTestMail(final Parameter _parameter)
        throws EFapsException
    {
        final Instance instance = _parameter.getInstance();
        final PrintQuery print = new PrintQuery(instance);
        print.addAttribute(CIMail.TemplateObject.Name);
        print.execute();

        final String template = print.getAttribute(CIMail.TemplateObject.Name);

        final Instance objInst = Instance
                        .get(_parameter.getParameterValue(CIFormMail.Mail_TemplateSendTestMailForm.instance.name));

        final Parameter parameter = ParameterUtil.clone(_parameter, ParameterValues.INSTANCE, objInst);

        ParameterUtil.setProperty(parameter, "Template", template);

        final SendMail sendMail = new SendMail();
        sendMail.sendObjectMail(parameter);

        return new Return();
    }
}
