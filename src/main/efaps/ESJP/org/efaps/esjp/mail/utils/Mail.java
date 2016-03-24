/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.esjp.mail.utils;

import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.api.annotation.EFapsSysConfAttribute;
import org.efaps.api.annotation.EFapsSystemConfiguration;
import org.efaps.esjp.admin.common.systemconfiguration.PropertiesSysConfAttribute;
import org.efaps.esjp.admin.common.systemconfiguration.StringSysConfAttribute;
import org.efaps.util.cache.CacheReloadException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@EFapsUUID("562ea5e7-49d0-4002-a606-7976947ec51e")
@EFapsApplication("eFapsApp-Mail")
@EFapsSystemConfiguration("510dc4fe-86a1-4317-b79d-149cdcf2c748")
public final class Mail
{
    /**
     * Base String to use Concatenation.
     */
    public static final String BASE = "org.efaps.mail.";

    /** Mail-Configuration. */
    public static final UUID SYSCONFUUID = UUID.fromString("510dc4fe-86a1-4317-b79d-149cdcf2c748");

    /** See description. */
    @EFapsSysConfAttribute
    public static final PropertiesSysConfAttribute SERVERS = new PropertiesSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "Config4Servers")
                    .concatenate(true)
                    .description("Configuration for Mail Server access. KEY. java mail properties e.g.\n"
                                    + "KEY.mail.smtp.user=USUARIO\n"
                                    + "KEY.mail.smtp.password=SUPERSECRETO\n"
                                    + "KEY.mail.smtp.host=smtp.gmail.com\n"
                                    + "KEY.mail.smtp.port=465\n"
                                    + "KEY.mail.debug=true\n"
                                    + "KEY.mail.smtp.starttls.enabled=true\n"
                                    + "KEY.mail.smtp.ssl.enable=true\n"
                                    + "KEY.mail.smtp.auth=true");

    /** See description. */
    @EFapsSysConfAttribute
    public static final StringSysConfAttribute DEFAULTFROM = new StringSysConfAttribute()
                    .sysConfUUID(SYSCONFUUID)
                    .key(BASE + "DefaultFrom")
                    .defaultValue("demo@efaps.org")
                    .description("Default send from email");

    /**
     * Singelton.
     */
    private Mail()
    {
    }

    /**
     * @return the SystemConfigruation for Mail
     * @throws CacheReloadException on error
     */
    public static SystemConfiguration getSysConfig()
        throws CacheReloadException
    {
        // Mail-Configuration
        return SystemConfiguration.get(SYSCONFUUID);
    }
}
