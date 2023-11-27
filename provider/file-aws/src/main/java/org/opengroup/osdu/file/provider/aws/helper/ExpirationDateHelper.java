/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.file.provider.aws.helper;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

/**
 * Needed to add this class to make it easy to unit test the creation of a date to the current time of running.
 */
public class ExpirationDateHelper {

    /**
     * Adds the timespan to the Local date and returns a Date object of that time.
     *
     * @param date     the start date
     * @param timeSpan a length of time to calculate the future date
     * @return expiration
     */
    public static Date getExpiration(Instant date, Duration timeSpan) {
        final Instant expiration = date.plus(timeSpan);

        return Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant());
    }
}
