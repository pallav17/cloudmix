/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller;

import java.net.URISyntaxException;

import java.util.List;

import com.sun.jersey.api.client.Client;

import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.common.dto.ProfileDetails;



/**
 * @version $Revision$
 */
public class ProfilesTest extends RuntimeTestSupport {
    private static final String PROFILE_ID1 = "d654017c-dcfe-43fc-be92-074457472660";
    private static final String PROFILE_ID2 = "b9903abc-8c57-4ebd-9aad-d9bda8bd1242";
    
    protected RestGridClient adminClient = new RestGridClient();

    public void testAddingAndRemovingProfiles() throws Exception {
        assertProfileSize(0, adminClient.getProfiles());

        adminClient.addProfile(new ProfileDetails(PROFILE_ID1));
        assertProfileSize(1, adminClient.getProfiles());
        assertProfileExists(PROFILE_ID1);

        adminClient.addProfile(new ProfileDetails(PROFILE_ID2));
        assertProfileSize(2, adminClient.getProfiles());
        assertProfileExists(PROFILE_ID2);

        LOG.info("Profiles: " + adminClient.getProfiles());

        Client client = new Client();
        String xml = client.resource(adminClient.getProfilesUri()).accept("text/xml").get(String.class);
        LOG.info("XML: " + xml);


        adminClient.removeProfile(PROFILE_ID1);
        adminClient.removeProfile(PROFILE_ID2);
        assertProfileSize(0, adminClient.getProfiles());
    }

    protected void assertProfileExists(String id) throws URISyntaxException {
        ProfileDetails details = adminClient.getProfile(id);
        assertNotNull("No profile found for id: " + id, details);
        assertEquals("profile.id", id, details.getId());
    }

    protected void assertProfileSize(int expectedSize, List<ProfileDetails> list) {
        assertEquals("List size: " + list, expectedSize, list.size());
    }
}