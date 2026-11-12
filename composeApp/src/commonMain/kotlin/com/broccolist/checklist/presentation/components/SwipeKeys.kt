package com.broccolist.checklist.presentation.components

import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem

/*
 * Keys for the checklist's single "currently swiped row" tracker.
 *
 * Sections and items live in separate DB tables, so their ids can collide
 * (section 5 and item 5). Prefixing keeps them apart. Rows that haven't been
 * saved yet (id == null while the insert is in flight) fall back to the same
 * natural key the ViewModel uses to de-duplicate them, so each pending row
 * still gets its own key instead of sharing a placeholder.
 */

internal val Section.swipeKey: String
    get() = id?.let { "section:$it" } ?: "section:pending:${section.displayName}"

internal val SectionItem.swipeKey: String
    get() = id?.let { "item:$it" } ?: "item:pending:$sectionId:$name"
