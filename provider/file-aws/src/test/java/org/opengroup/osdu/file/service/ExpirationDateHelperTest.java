// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.file.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.provider.aws.helper.ExpirationDateHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ExpirationDateHelperTest {

    @Test
    public void should_offset_time_by_duration() throws ParseException {
        long offSetInDays = 1;
        DateFormat dt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date start = dt.parse("01/01/2020 00:00:00");
        Duration span = Duration.ofDays(offSetInDays);

        LocalDateTime newDateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        Date actual = ExpirationDateHelper.getExpiration(start.toInstant(), span);
        Date expected = dt.parse("02/01/2020 00:00:00");

        assertEquals(expected, actual);
    }
}
