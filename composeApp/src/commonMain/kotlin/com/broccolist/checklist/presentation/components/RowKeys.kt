package com.broccolist.checklist.presentation.components

import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem

/*
 * Stable identity for checklist rows. Used as the LazyColumn key, the
 * drag-and-drop key, and the "currently swiped row" tracker.
 *
 * Sections and items live in separate DB tables, so their ids can collide
 * (section 5 and item 5). Prefixing keeps them apart.
 *
 * A row created on this device carries a client key from the moment it is
 * added, and keeps it after the insert returns and its id is filled in. Keying
 * on that first means the row is not torn down and rebuilt at that moment,
 * which would drop focus and lose an in-progress edit. Rows loaded from the
 * server have no client key and use their id.
 */

internal val Section.rowKey: String
    get() = clientKey?.let { "section:client:$it" }
        ?: id?.let { "section:$it" }
        ?: "section:pending:${section.displayName}"

internal val SectionItem.rowKey: String
    get() = clientKey?.let { "item:client:$it" }
        ?: id?.let { "item:$it" }
        ?: "item:pending:$sectionId:$name"
