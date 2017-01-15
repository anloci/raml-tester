/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.model.UnifiedApi;
import guru.nidi.ramltester.model.UnifiedResource;
import guru.nidi.ramltester.v10.SimpleTest;

import java.util.Arrays;

public final class SimpleReport {
    private SimpleReport() {
    }

    public static RamlReport report(String ramlfile, String... resources) {
        final UnifiedApi raml = RamlLoaders.fromClasspath(SimpleTest.class).load(ramlfile).getRaml();
        final RamlReport report = new RamlReport(raml);
        final Usage usage = report.getUsage();
        for (final UnifiedResource resource :raml.resources()) {
            if (Arrays.asList(resources).contains(resource.resourcePath())) {
                UsageBuilder.resourceUsage(usage, resource).incUses(1);
            }
        }
        return report;
    }
}
