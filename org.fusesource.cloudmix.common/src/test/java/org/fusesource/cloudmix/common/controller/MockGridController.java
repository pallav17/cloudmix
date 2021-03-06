/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.controller;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProfileStatus;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;


class MockGridController implements GridController {

    Map<String, FeatureController> fcs = new HashMap<String, FeatureController>();
    Map<String, Integer> featureInstancesCount = new HashMap<String, Integer>();

    public URI getRootUri() {
        return null;
    }

    public void addFeature(FeatureDetails featureDetails) {
        fcs.put(featureDetails.getId(), new FeatureController(this, featureDetails));
    }

    public FeatureController getFeatureController(Dependency dependency) {
        return fcs.get(dependency.getFeatureId());
    }

    public FeatureController getFeatureController(String featureId) {
        return fcs.get(featureId);
    }

    public String addAgentDetails(AgentDetails agentDetails) {
        return null;
    }

    public void addAgentToFeature(String featureId, String agentId, Map<String, String> cfgOverridesProps) {
    }

    public void addProfile(ProfileDetails profileDetails) {
    }

    public int evaluateIntegerExpression(String minimumInstances) {
        return minimumInstances == null ? 0 : Integer.parseInt(minimumInstances);
    }

    public AgentDetails getAgentDetails(String agentId) {
        return null;
    }

    public ProvisioningHistory getAgentHistory(String agentId) {
        return null;
    }

    public ProvisioningHistory pollAgentHistory(String agentId) {
        return null;
    }

    public long getAgentTimeout() {
        return 0;
    }

    public List<String> getAgentsAssignedToFeature(String featureId) {
        return null;
    }

    public List<String> getAgentsAssignedToFeature(String featureId,
                                                   String profileId,
                                                   boolean onlyIfDeployed) {
        return null;
    }

    public Collection<AgentDetails> getAllAgentDetails() {
        return null;
    }

    public Collection<FeatureDetails> getFeatureDetails() {
        return null;
    }

    public FeatureDetails getFeature(String featureId) {
        return null;
    }

    public int getFeatureInstanceCount(String id, String profileId, boolean onlyIfDeployed) {
        return featureInstancesCount.get(id) == null ? 0 : featureInstancesCount.get(id).intValue();
    }

    public List<FeatureDetails> getFeatures() {
        return null;
    }

    public List<ProfileDetails> getProfiles() {
        return null;
    }

    public ProfileDetails getProfile(String profileId) {
        return null;
    }

    public void removeAgentDetails(String agentId) {
    }

    public void removeAgentFromFeature(String featureId, String agentId) {
    }

    public void removeFeature(String featureId) {
    }

    public void removeProfile(String profileId) {
    }

    public void updateAgentDetails(String agentId, AgentDetails agentDetails) {
    }

    public ProfileStatus getProfileStatus(String profileId) {
        return null;
    }

    public Properties getProperties(String profileId) {
        return null;
    }

    public void removeFeature(FeatureDetails feature) {
    }

    public void removeProfile(ProfileDetails profile) {
    }
}