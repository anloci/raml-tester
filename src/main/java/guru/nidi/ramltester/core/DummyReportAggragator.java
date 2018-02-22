/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
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
 */
package guru.nidi.ramltester.core;

import java.util.Collections;
import java.util.Map;

public class DummyReportAggragator implements ReportAggregator {
    @Override
    public RamlReport addReport(RamlReport report) {
        return report;
    }

    @Override
    public Iterable<Map.Entry<String, Usage>> usages() {
        return Collections.<String, Usage>emptyMap().entrySet();
    }

    @Override
    public void clear() {
    }
}
