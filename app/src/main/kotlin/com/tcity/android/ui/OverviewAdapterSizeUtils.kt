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

class OverviewAdapterSizeUtils(
        public var section0DataSize: Int = 0,
        public var section1DataSize: Int = 0,
        public var section2DataSize: Int = 0,
        public var section3DataSize: Int = 0,
        public var section4DataSize: Int = 0,
        public var section5DataSize: Int = 0
) {
    public val section0Size: Int
        get() = plusOneIfNotZero(section0DataSize)

    public val section1Size: Int
        get() = plusOneIfNotZero(section1DataSize)

    public val section2Size: Int
        get() = plusOneIfNotZero(section2DataSize)

    public val section3Size: Int
        get() = plusOneIfNotZero(section3DataSize)

    public val section4Size: Int
        get() = plusOneIfNotZero(section4DataSize)

    public val section5Size: Int
        get() = plusOneIfNotZero(section5DataSize)

    public val size: Int
        get() = section0Size + section1Size + section2Size + section3Size + section4Size + section5Size

    public fun getSectionAndIndex(position: Int): Pair<Int, Int> {
        var currentPos = position

        if (currentPos < section0Size) return Pair(0, currentPos - 1)
        currentPos -= section0Size

        if (currentPos < section1Size) return Pair(1, currentPos - 1)
        currentPos -= section1Size

        if (currentPos < section2Size) return Pair(2, currentPos - 1)
        currentPos -= section2Size

        if (currentPos < section3Size) return Pair(3, currentPos - 1)
        currentPos -= section3Size

        if (currentPos < section4Size) return Pair(4, currentPos - 1)
        currentPos -= section4Size

        if (currentPos < section5Size) return Pair(5, currentPos - 1)

        throw IllegalArgumentException() // TODO
    }

    private fun plusOneIfNotZero(size: Int) = if (size == 0) 0 else size + 1
}
