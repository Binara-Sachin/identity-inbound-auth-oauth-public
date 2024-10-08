/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.webfinger.builders;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.webfinger.WebFingerEndpointException;
import org.wso2.carbon.identity.webfinger.WebFingerRequest;
import org.wso2.carbon.identity.webfinger.WebFingerResponse;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;

/**
 * Unit test coverage for WebFingerOIDCResponseBuilder class.
 */
@WithCarbonHome
public class WebFingerOIDCResponseBuilderTest {

    private WebFingerOIDCResponseBuilder webFingerOIDCResponseBuilder;
    private WebFingerRequest webFingerRequest;
    private final String oidcDiscoveryUrl = "https://oidc.testdomain.wso2.org:9443/oauth2/oidcdiscovery";
    private final String rel = "http://openid.net/specs/connect/1.0/issuer";
    private final String resource = "https://oidc.testdomain.wso2.org:9443/.well-known/webfinger";
    private final String host = "oidc.testdomain.wso2.org";
    private final String tenant = "carbon.super";
    private final String path = "/.well-known/webfinger";
    private final String scheme = "https";
    private final int port = 9443;

    private OAuthServerConfiguration mockOAuthServerConfiguration;
    private MockedStatic<OAuthServerConfiguration> oAuthServerConfiguration;
    private MockedStatic<OAuth2Util> oAuth2Util;

    @BeforeMethod
    public void setUp() throws Exception {

        webFingerOIDCResponseBuilder = new WebFingerOIDCResponseBuilder();
        webFingerRequest = new WebFingerRequest();
        webFingerRequest.setResource(resource);
        webFingerRequest.setHost(host);
        webFingerRequest.setScheme(scheme);
        webFingerRequest.setPort(port);
        webFingerRequest.setRel(rel);
        webFingerRequest.setTenant(tenant);

        mockOAuthServerConfiguration = mock(OAuthServerConfiguration.class);

        oAuthServerConfiguration = mockStatic(OAuthServerConfiguration.class);
        oAuthServerConfiguration.when(OAuthServerConfiguration::getInstance).thenReturn(mockOAuthServerConfiguration);
        oAuth2Util = mockStatic(OAuth2Util.class);
    }

    @AfterMethod
    public void tearDown() {

        oAuth2Util.close();
        oAuthServerConfiguration.close();
    }

    private void setPrivateStaticField(Class<?> clazz, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, newValue);
    }

    @Test
    public void testBuildWebFingerResponse() throws Exception {

        oAuth2Util.when(() -> OAuth2Util.getIssuerLocation(anyString())).thenReturn(oidcDiscoveryUrl);
        WebFingerResponse webFingerResponse = webFingerOIDCResponseBuilder.buildWebFingerResponse(webFingerRequest);
        assertEquals(webFingerResponse.getLinks().get(0).getRel(), rel, "rel is properly assigned");
        assertEquals(webFingerResponse.getLinks().get(0).getHref(), oidcDiscoveryUrl,
                "href is properly assigned");
        assertEquals(webFingerResponse.getSubject(), webFingerRequest.getResource(),
                "subject is properly assigned");
    }

    @Test(expectedExceptions = ServerConfigurationException.class)
    public void testBuildWebFingerException() throws WebFingerEndpointException, ServerConfigurationException,
            IdentityException {

        oAuth2Util.when(() -> OAuth2Util.getIssuerLocation(anyString())).thenThrow
                (new IdentityOAuth2Exception("Error"));
        webFingerOIDCResponseBuilder.buildWebFingerResponse(webFingerRequest);
    }
}
