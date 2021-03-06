/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.ui.material.demos

import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.foundation.selection.selectable
import androidx.ui.layout.*

import androidx.ui.material.RadioButton
import androidx.ui.material.samples.BottomNavigationSample
import androidx.ui.material.samples.BottomNavigationWithOnlySelectedLabelsSample
import androidx.ui.unit.dp

@Composable
fun BottomNavigationDemo() {
    var alwaysShowLabels by state { false }
    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .preferredHeight(56.dp)
                .selectable(
                    selected = !alwaysShowLabels,
                    onClick = { alwaysShowLabels = false }
                ),
            verticalGravity = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = !alwaysShowLabels,
                onClick = { alwaysShowLabels = false }
            )
            Spacer(Modifier.width(16.dp))
            Text("Only show labels when selected")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .preferredHeight(56.dp)
                .selectable(
                    selected = alwaysShowLabels,
                    onClick = { alwaysShowLabels = true }
                ),
            verticalGravity = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = alwaysShowLabels,
                onClick = { alwaysShowLabels = true }
            )
            Spacer(Modifier.width(16.dp))
            Text("Always show labels")
        }

        Spacer(Modifier.preferredHeight(50.dp))

        if (alwaysShowLabels) {
            BottomNavigationSample()
        } else {
            BottomNavigationWithOnlySelectedLabelsSample()
        }
    }
}
