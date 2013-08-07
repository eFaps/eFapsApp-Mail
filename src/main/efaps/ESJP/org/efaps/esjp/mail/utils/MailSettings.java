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


package org.efaps.esjp.mail.utils;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("7249df45-c013-4653-af13-e043ae9b2d4e")
@EFapsRevision("$Rev$")
public interface MailSettings
{
    /**
     * Base String to use Concatenation.
     */
    String BASE = "org.efaps.mail";

    /**
     * DBProperties.
     *
     * Base key. Must be used for Concatenation: e.g. "org.efaps.mail.server.GMAIL"
     *
     */
    String SERVER = MailSettings.BASE + ".server.";

}
