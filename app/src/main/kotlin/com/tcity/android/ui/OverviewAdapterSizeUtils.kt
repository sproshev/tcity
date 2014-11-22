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

package com.tcity.android.ui

class OverviewAdapterSizeUtils(dataSizes: Array<Int>) {

    private val sectionSizes = Array<Int>(dataSizes.size, { plusOneIfNotZero(dataSizes[it]) })

    public val size: Int
        get() = sectionSizes.sum()

    public fun setDataSize(index: Int, value: Int) {
        sectionSizes[index] = plusOneIfNotZero(value)
    }

    public fun getSectionAndIndex(position: Int): Pair<Int, Int> {
        var currentPos = position

        sectionSizes.indices.forEach {
            if (currentPos < sectionSizes[it]) return Pair(it, currentPos - 1)

            currentPos -= sectionSizes[it]
        }

        throw IllegalArgumentException("Illegal position: [position: $position, size: $size]")
    }

    private fun plusOneIfNotZero(size: Int) = if (size == 0) 0 else size + 1
}
