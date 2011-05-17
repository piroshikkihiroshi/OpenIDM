/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id$
 */

package org.forgerock.openidm.provisioner.openicf.commons;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.framework.common.objects.Uid;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Id is a util class to work with the {@code id} property in the {@link org.forgerock.openidm.objset.ObjectSet}
 * interface.
 * <p/>
 * A valid ID always MUST start with {@code system} and followed by the name of the end system, type of the
 * object. The last token is always the local identifier of the object instance.
 * <p/>
 * Valid identifiers:
 * {@code
 * system/LDAP/account
 * system/LDAP/account/
 * system/LDAP/account/ead738c0-7641-11e0-a1f0-0800200c9a66
 * system/Another+OpenIDM/account/http%3a%2f%2fopenidm%2fopenidm%2fmanaged%2fuser%2fead738c0-7641-11e0-a1f0-0800200c9a66
 * }
 * Invalid identifiers:
 * {@code
 * /system/LDAP/account
 * LDAP/account
 * system/account
 * }
 *
 * @author $author$
 * @version $Revision$ $Date$
 */
public class Id {
    public static final String SYSTEM_BASE = "system/";
    public static final String CHARACTER_ENCODING_UTF_8 = "UTF-8";
    private URI baseURI;
    private String systemName;
    private String objectType;
    private String localId = null;

    public Id(String systemName, String objectType) {
        baseURI = URI.create("");
        this.systemName = Assertions.blankChecked(systemName, "systemName");
        this.objectType = Assertions.blankChecked(objectType, "objectType");
    }

    public Id(String id) {
        int index = Assertions.blankChecked(id, "id").indexOf(SYSTEM_BASE);
        if (index > -1) {
            try {
                if (0 == index) {
                    baseURI = URI.create("");
                } else {
                    String baseURIString = id.substring(0, index);
                    URL baseContext = new URL(baseURIString);
                    baseURI = baseContext.toURI();
                }
                String identifiers = id.substring(index);
                String[] segments = identifiers.split("\\/");
                if (2 < segments.length && segments.length < 5) {
                    // fix length to 4
                    switch (segments.length > 3 ? 4 : segments.length) {
                        case 4:
                            localId = java.net.URLDecoder.decode(segments[3], CHARACTER_ENCODING_UTF_8);
                        case 3:
                            objectType = java.net.URLDecoder.decode(segments[2], CHARACTER_ENCODING_UTF_8);
                        case 2:
                            systemName = java.net.URLDecoder.decode(segments[1], CHARACTER_ENCODING_UTF_8);
                        case 1:
                            boolean isSystemId = "system".equals(segments[0]) ? true : false;
                    }
                } else {
                    throw new IllegalArgumentException("Invalid number of tokens in ID " + id);
                }
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid ID syntax of " + id, e);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid ID syntax of " + id + ". ID must start with \"system/\" " +
                        "or a valid URL syntax before \"system/\"", e);
            } catch (UnsupportedEncodingException e) {
                // Should never happen.
                throw new UndeclaredThrowableException(e);
            }
        } else {
            throw new IllegalArgumentException("Invalid ID syntax of " + id);
        }
    }

    public String getLocalId() {
        return localId;
    }

    public String getObjectType() {
        return objectType;
    }


    public URI resolveLocalId(Uid uid) {
        try {
            URI id = getObjectSetId();
            if (null != uid) {
                id = id.resolve(URLEncoder.encode(uid.getUidValue(), CHARACTER_ENCODING_UTF_8));
            }
            return id;
        } catch (UnsupportedEncodingException e) {
            // Should never happen.
            throw new UndeclaredThrowableException(e);
        }
    }

    public URI getId() {
        try {
            URI id = getObjectSetId();
            if (null != localId) {
                id = id.resolve(URLEncoder.encode(localId, CHARACTER_ENCODING_UTF_8));
            }
            return id;
        } catch (UnsupportedEncodingException e) {
            // Should never happen.
            throw new UndeclaredThrowableException(e);
        }
    }

    private URI getObjectSetId() throws UnsupportedEncodingException {
        return baseURI.resolve(SYSTEM_BASE).resolve(URLEncoder.encode(systemName, CHARACTER_ENCODING_UTF_8) + "/").resolve(URLEncoder.encode(objectType, CHARACTER_ENCODING_UTF_8) + "/");
    }

    @Override
    public String toString() {
        URI id = baseURI.resolve(SYSTEM_BASE).resolve(systemName).resolve(objectType);
        if (null != localId) {
            return id.toString() + "/ " + localId;
        }
        return id.toString();
    }

    /**
     * Safely escapes the {@link Uid} value
     *
     * @param uid
     * @return
     * @throws IllegalArgumentException if the {@code uid} is blank
     * @throws NullPointerException     if the {@code uid} is null
     */
    public static String escapeUid(Uid uid) {
        Assertions.nullCheck(uid, "uid");
        try {
            return URLEncoder.encode(Assertions.blankChecked(uid.getUidValue(), "uid"), CHARACTER_ENCODING_UTF_8);
        } catch (UnsupportedEncodingException e) {
            // Should never happen.
            throw new UndeclaredThrowableException(e);
        }
    }
}
