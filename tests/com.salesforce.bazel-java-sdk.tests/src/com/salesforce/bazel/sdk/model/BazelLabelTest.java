/**
 * Copyright (c) 2019, Salesforce.com, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Salesforce.com nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */
package com.salesforce.bazel.sdk.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.salesforce.bazel.sdk.path.BazelPathHelper;

public class BazelLabelTest {

    @Test
    public void testGetRepository() {
        assertEquals("foo", new BazelLabel("@foo//a/b/c:t1").getRepositoryName()); // $SLASH_OK bazel path
        assertNull(new BazelLabel("//a/b/c").getRepositoryName()); // $SLASH_OK bazel path
    }

    @Test
    public void testIsPackageDefault() {
        assertTrue(new BazelLabel("foo").isPackageDefault());
        assertTrue(new BazelLabel("//foo/blah").isPackageDefault()); // $SLASH_OK bazel path
        assertFalse(new BazelLabel("blah:t").isPackageDefault());
        assertFalse(new BazelLabel("//:query").isPackageDefault());
        assertFalse(new BazelLabel("//...").isPackageDefault());
        assertFalse(new BazelLabel("//:*").isPackageDefault());
        assertTrue(new BazelLabel("@foo//foo/blah").isPackageDefault()); // $SLASH_OK bazel path
    }

    @Test
    public void testIsConcrete() {
        assertTrue(new BazelLabel("//foo/blah").isConcrete()); // $SLASH_OK bazel path
        assertTrue(new BazelLabel("//foo/blah:t1").isConcrete()); // $SLASH_OK bazel path
        assertFalse(new BazelLabel("blah/...").isConcrete()); // $SLASH_OK bazel path
        assertFalse(new BazelLabel("blah:*").isConcrete());
        assertFalse(new BazelLabel("blah:all").isConcrete());
        assertTrue(new BazelLabel("//:query").isConcrete());
        assertFalse(new BazelLabel("//...").isConcrete());
        assertFalse(new BazelLabel("//:*").isConcrete());
        assertFalse(new BazelLabel("//:all").isConcrete());
        assertTrue(new BazelLabel("@foo//query:t2").isConcrete());
    }

    @Test
    public void testGetPackagePath() {
        assertEquals("foo", new BazelLabel("foo").getPackagePath());
        assertEquals("foo/blah", new BazelLabel("//foo/blah").getPackagePath()); // $SLASH_OK bazel path
        assertEquals("foo/blah", new BazelLabel("foo/blah:t1").getPackagePath()); // $SLASH_OK bazel path
        assertEquals("blah", new BazelLabel("blah/...").getPackagePath()); // $SLASH_OK bazel path
        assertEquals("blah", new BazelLabel("blah:*").getPackagePath());
        assertEquals("", new BazelLabel("//:query").getPackagePath());
        assertEquals("", new BazelLabel("//...").getPackagePath());
        assertEquals("", new BazelLabel("//:*").getPackagePath());
        assertEquals("", new BazelLabel("//:all").getPackagePath());
        assertEquals("a/b/c", new BazelLabel("@foo//a/b/c").getPackagePath()); // $SLASH_OK bazel path
    }

    @Test
    public void testToDefaultPackageLabel() {
        assertEquals("//foo", new BazelLabel("foo").toDefaultPackageLabel().getLabel());
        assertEquals("//foo/blah", new BazelLabel("//foo/blah").toDefaultPackageLabel().getLabel()); // $SLASH_OK bazel path
        assertEquals("//foo/blah", new BazelLabel("foo/blah:t1").toDefaultPackageLabel().getLabel()); // $SLASH_OK bazel path
        assertEquals("//blah", new BazelLabel("blah/...").toDefaultPackageLabel().getLabel()); // $SLASH_OK bazel path
        assertEquals("//blah", new BazelLabel("blah:*").toDefaultPackageLabel().getLabel());
        assertEquals("@foo//blah/goo", new BazelLabel("@foo//blah/goo:t1").toDefaultPackageLabel().getLabel()); // $SLASH_OK bazel path
    }

    @Test
    public void testInvalidButAcceptableInput() {
        // we fixup the input for these cases
        assertEquals("//foo:t1", new BazelLabel("/foo:t1").getLabel()); // $SLASH_OK bazel path
        assertEquals("//foo:t1", new BazelLabel("foo", ":t1").getLabel());
        assertEquals("//foo:t1", new BazelLabel("foo/", "t1").getLabel()); // $SLASH_OK bazel path
        assertEquals("//foo:t1", new BazelLabel("/foo/", "t1").getLabel()); // $SLASH_OK bazel path
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToDefaultPackageLabel_invalidRootLabel() {
        new BazelLabel("//...").toDefaultPackageLabel();
    }

    @Test
    public void testGetPackageName() {
        assertEquals("foo", new BazelLabel("foo").getPackageName());
        assertEquals("blah", new BazelLabel("//foo/blah").getPackageName()); // $SLASH_OK bazel path
        assertEquals("blah", new BazelLabel("foo/blah:t1").getPackageName()); // $SLASH_OK bazel path
        assertEquals("blah", new BazelLabel("blah/...").getPackageName()); // $SLASH_OK bazel path
        assertEquals("blah", new BazelLabel("blah:*").getPackageName());
        assertEquals("", new BazelLabel("//:query").getPackageName());
        assertEquals("", new BazelLabel("//...").getPackageName());
        assertEquals("", new BazelLabel("//:*").getPackageName());
        assertEquals("goo", new BazelLabel("@foo//blah/goo").getPackageName()); // $SLASH_OK bazel path
    }

    @Test
    public void testGetTargetName() {
        assertEquals("foo", new BazelLabel("foo").getTargetName());
        assertEquals("blah", new BazelLabel("//foo/blah").getTargetName()); // $SLASH_OK bazel path
        assertEquals("t1", new BazelLabel("foo/blah:t1").getTargetName()); // $SLASH_OK bazel path
        assertEquals(null, new BazelLabel("blah/...").getTargetName()); // $SLASH_OK bazel path
        assertEquals("*", new BazelLabel("blah:*").getTargetName());
        assertEquals("all", new BazelLabel("blah:all").getTargetName());
        assertEquals("query", new BazelLabel("//:query").getTargetName());
        assertEquals(null, new BazelLabel("//...").getTargetName());
        assertEquals("*", new BazelLabel("//:*").getTargetName());
        assertEquals("all", new BazelLabel("//:all").getTargetName());
        assertEquals("t1", new BazelLabel("@foo//blah/goo:t1").getTargetName()); // $SLASH_OK bazel path
    }

    @Test
    public void testGetLabel() {
        assertEquals("//foo", new BazelLabel("foo").getLabel());
        assertEquals("//foo/blah", new BazelLabel("//foo/blah").getLabel()); // $SLASH_OK bazel path
        assertEquals("//foo/blah:t1", new BazelLabel("foo/blah:t1").getLabel()); // $SLASH_OK bazel path
        assertEquals("//:query", new BazelLabel("//:query").getLabel());
        assertEquals("//...", new BazelLabel("//...").getLabel());
        assertEquals("//:*", new BazelLabel("//:*").getLabel());
        assertEquals("@foo//blah/goo:t1", new BazelLabel("@foo//blah/goo:t1").getLabel()); // $SLASH_OK bazel path
    }

    @Test
    public void testToPackageWildcardLabel() {
        assertEquals("//foo:*", new BazelLabel("foo").toPackageWildcardLabel().getLabel());
        assertEquals("@repo//foo:*", new BazelLabel("@repo//foo").toPackageWildcardLabel().getLabel());
    }

    @Test
    public void testGetLastComponentOfTargetName() {
        assertEquals("test123", new BazelLabel("test123").getLastComponentOfTargetName());
        assertEquals("test123", new BazelLabel("foo:test123").getLastComponentOfTargetName());
        assertEquals("blah", new BazelLabel("foo:src/foo/blah").getLastComponentOfTargetName()); // $SLASH_OK bazel path
        assertEquals("query", new BazelLabel("//:query").getLastComponentOfTargetName());
        assertEquals(null, new BazelLabel("//...").getLastComponentOfTargetName());
        assertEquals("blah", new BazelLabel("@repo//foo:src/foo/blah").getLastComponentOfTargetName()); // $SLASH_OK bazel path
    }

    @Test
    public void testPackageAndTargetCtor() {
        assertEquals("//a/b/c:foo", new BazelLabel("a/b/c", "foo").getLabel()); // $SLASH_OK bazel path
        assertEquals("//a/b/c:foo", new BazelLabel("//a/b/c", "foo").getLabel()); // $SLASH_OK bazel path
    }

    @Test
    public void testEquality() {
        Set<BazelLabel> s = new HashSet<>();
        s.add(new BazelLabel("//a/b/c")); // $SLASH_OK bazel path
        s.add(new BazelLabel("//a/b/c")); // $SLASH_OK bazel path
        s.add(new BazelLabel("@repo//a/b/c")); // $SLASH_OK bazel path

        assertEquals(2, s.size());
        assertTrue(s.contains(new BazelLabel("//a/b/c"))); // $SLASH_OK bazel path
        assertTrue(s.contains(new BazelLabel("@repo//a/b/c"))); // $SLASH_OK bazel path
    }

    @Test(expected = IllegalStateException.class)
    public void testToPackageWildcardLabel_invalidLabel() {
        assertEquals("//foo:*", new BazelLabel("foo:test").toPackageWildcardLabel().getLabel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLabel_null() {
        new BazelLabel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLabel_empty() {
        new BazelLabel("  ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLabel_trailingColon() {
        new BazelLabel("//blah:");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLabel_trailingSlash() {
        new BazelLabel(BazelPathHelper.BAZEL_SLASH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLabel_onlyLeadingSlashes() {
        new BazelLabel("//");
    }
}
