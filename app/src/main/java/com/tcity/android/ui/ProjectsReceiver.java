/*
 * Copyright 2014 Semyon Proshev
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

package com.tcity.android.ui;

import com.tcity.android.Receiver;
import com.tcity.android.concept.Project;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Collection;

class ProjectsReceiver extends Receiver<Collection<Project>> {

    @NotNull
    private final WeakReference<ViewReceiver> myReceiverWeakReference;

    @NotNull
    private final WeakReference<OverviewCalculator> myCalculatorWeakReference;

    ProjectsReceiver(@NotNull ViewReceiver receiver, @NotNull OverviewCalculator calculator) {
        myReceiverWeakReference = new WeakReference<>(receiver);
        myCalculatorWeakReference = new WeakReference<>(calculator);
    }

    @Override
    public void handleResult(@NotNull Collection<Project> projects) {
        ViewReceiver receiver = myReceiverWeakReference.get();
        OverviewCalculator calculator = myCalculatorWeakReference.get();

        if (receiver == null || calculator == null) {
            return;
        }

        calculator.updateProjects(projects, receiver);
    }

    @Override
    public void handleException(@NotNull Exception e) {
        ViewReceiver receiver = myReceiverWeakReference.get();

        if (receiver == null) {
            return;
        }

        receiver.receiveException(e);
    }
}
