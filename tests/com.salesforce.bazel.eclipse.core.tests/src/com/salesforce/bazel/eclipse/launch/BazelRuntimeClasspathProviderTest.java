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
 */
package com.salesforce.bazel.eclipse.launch;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import com.salesforce.bazel.sdk.util.BazelPathHelper;

public class BazelRuntimeClasspathProviderTest {

    @Test
    public void getPathsToJars() {
        BazelRuntimeClasspathProvider subject = new BazelRuntimeClasspathProvider();
        List<String> result = subject.getPathsToJars(new Scanner(PARAM_FILE_CONTENTS));

        String deployPath = BazelPathHelper.osSeps(
            "bazel-out/darwin-fastbuild/bin/projects/libs/banana/banana-api/src/test/java/demo/banana/api/BananaTest_deploy.jar"); // $SLASH_OK
        String libPath = BazelPathHelper
                .osSeps("bazel-out/darwin-fastbuild/bin/projects/libs/banana/banana-api/libbanana-api.jar"); // $SLASH_OK
        String slf4jPath = BazelPathHelper.osSeps("external/org_slf4j_slf4j_api/jar/slf4j-api-1.7.25.jar"); // $SLASH_OK
        String guavaPath = BazelPathHelper.osSeps("external/com_google_guava_guava/jar/guava-20.0.jar"); // $SLASH_OK
        String junitPath = BazelPathHelper.osSeps("external/junit_junit/jar/junit-4.12.jar"); // $SLASH_OK
        String runnerPath = BazelPathHelper.osSeps("external/remote_java_tools/java_tools/Runner_deploy.jar"); // $SLASH_OK
        String testPath = BazelPathHelper.osSeps(
            "bazel-out/darwin-fastbuild/bin/projects/libs/banana/banana-api/src/test/java/demo/banana/api/BananaTest.jar"); // $SLASH_OK

        assertEquals(7, result.size());
        assertEquals(deployPath, result.get(0));
        assertEquals(libPath, result.get(1));
        assertEquals(slf4jPath, result.get(2));
        assertEquals(guavaPath, result.get(3));
        assertEquals(junitPath, result.get(4));
        assertEquals(runnerPath, result.get(5));
        assertEquals(testPath, result.get(6));
    }

    @Test
    public void getParamsJarSuffix() {
        BazelRuntimeClasspathProvider subject = new BazelRuntimeClasspathProvider();
        assertEquals("_deploy.jar-0.params", subject.getParamsJarSuffix(false));
        assertEquals("_deploy-src.jar-0.params", subject.getParamsJarSuffix(true));
    }

    private static String PARAM_FILE_CONTENTS = "--output\n" + BazelPathHelper.osSeps(
        "bazel-out/darwin-fastbuild/bin/projects/libs/banana/banana-api/src/test/java/demo/banana/api/BananaTest_deploy.jar\n") // $SLASH_OK
            + "--compression\n" + "--normalize\n" + "--main_class\n"
            + "com.google.testing.junit.runner.BazelTestRunner\n" + "--build_info_file\n"
            + BazelPathHelper.osSeps("bazel-out/darwin-fastbuild/include/build-info-redacted.properties\n") // $SLASH_OK
            + "--sources\n"
            + BazelPathHelper.osSeps("bazel-out/darwin-fastbuild/bin/projects/libs/banana/banana-api/libbanana-api.jar") // $SLASH_OK
            + ",//projects/libs/banana/banana-api:banana-api\n"
            + BazelPathHelper.osSeps("external/org_slf4j_slf4j_api/jar/slf4j-api-1.7.25.jar") // $SLASH_OK
            + ",@@org_slf4j_slf4j_api//jar:slf4j-api-1.7.25.jar\n"
            + BazelPathHelper.osSeps("external/com_google_guava_guava/jar/guava-20.0.jar") // $SLASH_OK
            + ",@@com_google_guava_guava//jar:guava-20.0.jar\n"
            + BazelPathHelper.osSeps("external/junit_junit/jar/junit-4.12.jar") + ",@@junit_junit//jar:junit-4.12.jar\n" // $SLASH_OK
            + BazelPathHelper.osSeps("external/remote_java_tools/java_tools/Runner_deploy.jar") // $SLASH_OK
            + ",@@remote_java_tools//:java_tools/Runner_deploy.jar\n"
            + BazelPathHelper.osSeps(
                "bazel-out/darwin-fastbuild/bin/projects/libs/banana/banana-api/src/test/java/demo/banana/api/BananaTest.jar") // $SLASH_OK
            + ",//projects/libs/banana/banana-api:src/test/java/demo/banana/api/BananaTest\n" + "";
}
