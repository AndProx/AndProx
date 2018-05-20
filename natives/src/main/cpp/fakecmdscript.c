/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2017 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Under section 7 of the GNU General Public License v3, the following additional
 * terms apply to this program:
 *
 *  (b) You must preserve reasonable legal notices and author attributions in
 *      the program.
 *  (c) You must not misrepresent the origin of this program, and need to mark
 *      modified versions in reasonable ways as different from the original
 *      version (such as changing the name and logos).
 *  (d) You may not use the names of licensors or authors for publicity
 *      purposes, without explicit written permission.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "proxmark3.h"
#include "data.h"
#include "ui.h"
#include "graph.h"
#include "cmdparser.h"
#include "cmdmain.h"
#include "cmdscript.h"

static int CmdHelp(const char *Cmd);
static int CmdList(const char *Cmd);
static int CmdRun(const char *Cmd);

command_t CommandTable[] =
        {
                {"help",  CmdHelp, 1, "Scripting disabled on Android"},
                {NULL, NULL, 0, NULL}
        };


/**
 * Shows some basic help
 * @brief CmdHelp
 * @param Cmd
 * @return
 */
int CmdHelp(const char * Cmd)
{
    PrintAndLog("Scripting is disabled on Android");
    return 0;
}

/**
 * Finds a matching script-file
 * @brief CmdScript
 * @param Cmd
 * @return
 */
int CmdScript(const char *Cmd)
{
    CmdsParse(CommandTable, Cmd);
    return 0;
}

