-- Phase 2: drag-and-drop ordering.
--
-- Gives sections (ordered within a list) and items (ordered within a section)
-- a fractional `position`. A moved row takes the midpoint of its new
-- neighbours, so a drop is a single-row update.
--
-- The live database already had a `position bigint` column on both tables
-- when Phase 2 started. A
-- bigint can't hold a midpoint, so this converts it; on a database without
-- the column it creates it.
--
-- Run once in the Supabase SQL editor. Re-running is safe: it renumbers rows
-- to 1, 2, 3, ... in their current order, which changes values but not order.

begin;

alter table public.sections add column if not exists position double precision;
alter table public.items    add column if not exists position double precision;

-- The existing column carried a table-wide unique constraint
-- (`sections_index_key`). Positions are only meaningful within one list or
-- section, so every list legitimately has a section at position 1. Drop any
-- unique constraint that covers `position` alone, whatever it is named.
do $$
declare
    c record;
begin
    for c in
        select rel.relname, con.conname
        from pg_constraint con
        join pg_class rel on rel.oid = con.conrelid
        join pg_namespace ns on ns.oid = rel.relnamespace
        where ns.nspname = 'public'
          and rel.relname in ('sections', 'items')
          and con.contype = 'u'
          and con.conkey = array[(
              select att.attnum
              from pg_attribute att
              where att.attrelid = rel.oid and att.attname = 'position'
          )]
    loop
        execute format('alter table public.%I drop constraint %I', c.relname, c.conname);
    end loop;
end $$;

-- An identity column can't change type or take a plain default.
alter table public.sections alter column position drop identity if exists;
alter table public.items    alter column position drop identity if exists;

alter table public.sections alter column position type double precision using position::double precision;
alter table public.items    alter column position type double precision using position::double precision;

-- Renumber every container. Existing positions win; rows that tie or have
-- none fall back to id order, which is the order they were created in and
-- (roughly) what the app showed before.
update public.sections s
set position = ranked.rn
from (
    select id,
           row_number() over (partition by list_id order by position nulls last, id) as rn
    from public.sections
) ranked
where s.id = ranked.id;

update public.items i
set position = ranked.rn
from (
    select id,
           row_number() over (partition by section_id order by position nulls last, id) as rn
    from public.items
) ranked
where i.id = ranked.id;

-- The client always sends a position on insert; the default only covers rows
-- written by an older build.
alter table public.sections alter column position set default 0;
alter table public.items    alter column position set default 0;
alter table public.sections alter column position set not null;
alter table public.items    alter column position set not null;

create index if not exists sections_list_position_idx
    on public.sections (list_id, position);
create index if not exists items_section_position_idx
    on public.items (section_id, position);

commit;
