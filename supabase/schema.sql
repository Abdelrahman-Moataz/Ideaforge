-- Run this in Supabase Dashboard > SQL Editor.
-- User data is isolated by auth.uid() through Row Level Security.

create table if not exists public.ideas (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    name text not null,
    problem text not null,
    audience text not null default '',
    solution text not null default '',
    category text not null default 'Personal',
    notes text not null default '',
    stage text not null default 'Captured',
    tasks jsonb not null default '[]'::jsonb,
    created_at timestamptz not null default now()
);

alter table public.ideas enable row level security;

drop policy if exists "Users can view their own ideas" on public.ideas;
create policy "Users can view their own ideas"
on public.ideas for select to authenticated
using (auth.uid() = user_id);

drop policy if exists "Users can create their own ideas" on public.ideas;
create policy "Users can create their own ideas"
on public.ideas for insert to authenticated
with check (auth.uid() = user_id);

drop policy if exists "Users can update their own ideas" on public.ideas;
create policy "Users can update their own ideas"
on public.ideas for update to authenticated
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

drop policy if exists "Users can delete their own ideas" on public.ideas;
create policy "Users can delete their own ideas"
on public.ideas for delete to authenticated
using (auth.uid() = user_id);

grant select, insert, update, delete on public.ideas to authenticated;
