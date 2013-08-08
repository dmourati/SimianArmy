/*
 *
 *  Copyright 2012 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.netflix.simianarmy.aws.janitor.rule.instance;

import com.netflix.simianarmy.Resource;
import com.netflix.simianarmy.aws.AWSResource;
import com.netflix.simianarmy.janitor.JanitorMonkey;
import com.netflix.simianarmy.janitor.Rule;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The rule for checking the tags associated with an instance.
 */
public class TaggedInstanceRule implements Rule {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TaggedInstanceRule.class);

    @SuppressWarnings("UnusedDeclaration")
    private static final String TERMINATION_REASON = "Tag(s) associated with this instance";

    /**
     * Constructor for TaggedInstanceRule.
     *
     */
    @SuppressWarnings("UnusedDeclaration")
    public TaggedInstanceRule() {
    }

    @Override
    public boolean isValid(Resource resource) {
        Validate.notNull(resource);
        if (!resource.getResourceType().name().equals("INSTANCE")) {
            // The rule is supposed to only work on AWS instances. If a non-instance resource
            // is passed to the rule, the rule simply ignores it and considers it as a valid
            // resource not for cleanup.
            return true;
        }
        String awsStatus = ((AWSResource) resource).getAWSResourceState();
        if (!"running".equals(awsStatus) || "pending".equals(awsStatus)) {
            return true;
        }
        // The instance can have a special tag is it wants to be cleaned up
        // by Janitor monkey.

        // if the instance is labelled cleanup=true
        String cleanupTag = resource.getTag(JanitorMonkey.CLEANUP_TAG);
        if (cleanupTag != null) {
            if ("true".equals(cleanupTag)) {
                LOGGER.info(String.format("The instance %s tagged as cleanup handled by Janitor", resource.getId()));
                return false;
            }   else   {
                LOGGER.info(String.format("The instance %s is not tagged for cleanup handled by Janitor.", resource.getId()));
                return true;
            }
        } else
            return true;
    }
}