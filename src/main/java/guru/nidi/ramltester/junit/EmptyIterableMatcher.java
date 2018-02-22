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
package guru.nidi.ramltester.junit;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class EmptyIterableMatcher extends TypeSafeMatcher<Iterable<?>> {
    private final String desc;

    public EmptyIterableMatcher(String desc) {
        this.desc = desc;
    }

    @Override
    protected boolean matchesSafely(Iterable<?> item) {
        return !item.iterator().hasNext();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(desc + " to be empty");
    }
}
