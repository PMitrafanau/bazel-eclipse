/**
 * Copyright (c) 2020, Salesforce.com, Inc. All rights reserved.
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
package com.salesforce.bazel.eclipse.projectimport.flow;

import java.io.File;
import java.util.Objects;

import org.eclipse.core.runtime.SubMonitor;

import com.salesforce.bazel.eclipse.BazelPluginActivator;
import com.salesforce.bazel.eclipse.projectimport.ProjectImporterFactory;
import com.salesforce.bazel.sdk.command.BazelWorkspaceCommandOptions;
import com.salesforce.bazel.sdk.lang.jvm.JavaLanguageLevelHelper;
import com.salesforce.bazel.sdk.model.BazelPackageLocation;
import com.salesforce.bazel.sdk.model.BazelWorkspace;
import com.salesforce.bazel.sdk.util.BazelPathHelper;
import com.salesforce.bazel.sdk.workspace.BazelWorkspaceScanner;

/**
 * Import initialization type work.
 */
public class InitImportFlow implements ImportFlow {

    @Override
    public String getProgressText() {
        return "Preparing import";
    }

    @Override
    public void assertContextState(ImportContext ctx) {
        Objects.requireNonNull(ctx.getSelectedBazelPackages());
        Objects.requireNonNull(ctx.getBazelWorkspaceRootPackageInfo());
    }

    @Override
    public void run(ImportContext ctx, SubMonitor progressMonitor) {
        ProjectImporterFactory.importInProgress.set(true);

        File bazelWorkspaceRootDirectory = initContext(ctx);

        BazelWorkspace bazelWorkspace = initBazelWorkspace(bazelWorkspaceRootDirectory);

        initWorkspaceOptions(ctx, bazelWorkspace);

        warmupCaches(bazelWorkspace);
    }

    @Override
    public void finish(ImportContext ctx) {
        ProjectImporterFactory.importInProgress.set(false);
    }

    private static void warmupCaches(BazelWorkspace bazelWorkspace) {
        // these are cached - initialize them now so we do not incur the cost of determining these locations
        // later when creating projects
        bazelWorkspace.getBazelOutputBaseDirectory();
        bazelWorkspace.getBazelExecRootDirectory();
    }

    private static void initWorkspaceOptions(ImportContext ctx, BazelWorkspace bazelWorkspace) {
        // get the Workspace options (.bazelrc)
        // note that this ends up running bazel (bazel test --announce_rc)
        BazelWorkspaceCommandOptions options = bazelWorkspace.getBazelWorkspaceCommandOptions();
        // determine the Java levels
        String javacoptString = options.getContextualOption("build", "javacopt");
        int sourceLevel = JavaLanguageLevelHelper.getSourceLevelAsInt(javacoptString);
        ctx.setJavaLanguageLevel(sourceLevel);
    }

    private static File initContext(ImportContext ctx) {
        BazelPackageLocation bazelWorkspaceRootPackageInfo = ctx.getBazelWorkspaceRootPackageInfo();
        File bazelWorkspaceRootDirectory =
                BazelPathHelper.getCanonicalFileSafely(bazelWorkspaceRootPackageInfo.getWorkspaceRootDirectory());
        ctx.init(bazelWorkspaceRootDirectory);
        return bazelWorkspaceRootDirectory;
    }

    private static BazelWorkspace initBazelWorkspace(File bazelWorkspaceRootDirectory) {
        BazelWorkspace bazelWorkspace = BazelPluginActivator.getBazelWorkspace();
        boolean isInitialImport = bazelWorkspace == null;
        String bazelWorkspaceName = null;
        if (isInitialImport) {
            bazelWorkspaceName = BazelWorkspaceScanner.getBazelWorkspaceName(bazelWorkspaceRootDirectory.getName());

            // Many collaborators need the Bazel workspace directory location, so we stash it in an accessible global location
            // currently we only support one Bazel workspace in an Eclipse workspace
            BazelPluginActivator.getInstance().setBazelWorkspaceRootDirectory(bazelWorkspaceName,
                bazelWorkspaceRootDirectory);
        } else {
            bazelWorkspaceName = bazelWorkspace.getName();
        }

        bazelWorkspace = BazelPluginActivator.getBazelWorkspace();

        return bazelWorkspace;
    }
}
