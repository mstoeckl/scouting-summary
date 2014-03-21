#!/bin/sh python3

import re

fields = ["Team","Match"
          ,"AutoHighHot","AutoHighCold","AutoLowHot","AutoLowCold","AutoMiss"
          ,"TrussField","TrussHP","TrussFail"
          ,"High","HighMiss"
          ,"MiscFoul","MiscStuckBall","MiscDeadBot"
          ,"Pickups","LowGoal"
          ,"EtcMobility","EtcNoShow","EtcDefending","EtcDefended","EtcGoalie","EtcGoalieBlock"]

incoming = ["Team","Match","Match Type","No Show"
            ,"A Start Pos","A High","A Low","A Missed"
            ,"A Ball","A Hot","A Mobility","A Goalie Block"
            ,"High","Low","Truss","Catch","Pickup","Off Missed/Blocked"
            ,"Def Missed/Blocked","Foul","Tech Foul","Truss to HP"
            ,"Played D","Had D","Stuck","Dead"]

def readcsv(filename):
    with open(filename) as f:
        lines = f.readlines()

    header = lines[0].rstrip("\n").split(",")
    rest = [x.rstrip("\"\n").lstrip("\"").split("\",\"") for x in lines[1:]]

    return header, rest

lastmode = "Qualification"
def modecv(blob):
    global lastmode
    if blob == "none":
        return lastmode

    s = blob.split(",")
    if len(s) != 1 and lastmode in s:
            return lastmode
    lastmode = s[0]
    return s[0]


lastmatch = 0
matchcount = 0
def matchcv(blob):
    global lastmatch
    global matchcount

    # Each digit can be converted into three things:
    #   a digit (0-9)
    #   a list of possible digits (0,5,9)
    #   "x", if unknown
    #
    # Our goal is, knowing that matches arrive to this function in a sorted
    # fashion, with at most 6 matches per match number, to recreate the number.
    # Gaps in the match record may exist.
    #
    # Oh, and the scanner is partial to nines in the last digit, so
    # we do some checking for that when multiple options are available.
    # It really loves those nines.


    # Split
    v = []
    k = 0
    for i in range(len(blob)-1):
        if blob[i] == "x":
            v.append(blob[k:i+1])
            k = i + 1
        elif blob[i+1] == "x":
            v.append(blob[k:i+1])
            k = i + 1
        if blob[i].isnumeric() and blob[i+1].isnumeric():
            v.append(blob[k:i+1])
            k = i+1
    v.append(blob[k:])

    # Process
    def ipack(i):
        s = list(map(int,str(i)))
        return [0] * (3 - len(s)) + s

    if matchcount > 6:
        likely1 = ipack(lastmatch+1)
        likely2 = ipack(lastmatch)
    else:
        likely1 = ipack(lastmatch)
        likely2 = ipack(lastmatch+1)

    out = []
    last = ipack(lastmatch)
    for i in range(3):
        if v[i] == "x":
            out.append(likely1[i])
        elif len(v[i]) == 1:
            out.append(int(v[i]))
        else:
            opt = list(map(int, v[i].split(",")))
            carry = (i == 0 or last[i-1] == out[i-1])
            if carry and likely1[i] in opt:
                out.append(likely1[i])
            elif carry and likely2[i] in opt:
                out.append(likely2[i])
            else:
                if 9 in opt:
                    opt.remove(9)
                out.append(max(opt))


    # Collate and clean
    result = out[0] * 100 + out[1] * 10 + out[2]
    if lastmatch == result:
        matchcount += 1
    else:
        matchcount = 0
    lastmatch = result
    return result

def iconv(blob):
    try:
        return max(int(j) for j in blob.split(","))
    except:
        return -1
def bconv(blob):
    if blob == "Yes":
        return 1
    elif blob == "No":
        return 0


def convert(line, header):
    d = dict(zip(header, line))

    o = {}

    md = modecv(d["Match Type"])

    o["Team"] = iconv(d["Team"])
    o["Match"] = matchcv(d["Match"])
    if bconv(d["A Hot"]):
        o["AutoHighHot"] = iconv(d["A High"])
        o["AutoLowHot"] = iconv(d["A Low"])
        o["AutoHighCold"] = 0
        o["AutoLowCold"] = 0
    else:
        o["AutoHighHot"] = 0
        o["AutoLowHot"] = 0
        o["AutoHighCold"] = iconv(d["A High"])
        o["AutoLowCold"] = iconv(d["A Low"])
    o["AutoMiss"] = iconv(d["A Missed"])
    if bconv(d["Truss to HP"]):
        o["TrussHP"] = iconv(d["Truss"])
        o["TrussField"] = 0
    else:
        o["TrussHP"] = 0
        o["TrussField"] = iconv(d["Truss"])
    o["TrussFail"] = 0
    o["High"] = iconv(d["High"])
    o["HighMiss"] = iconv(d["Off Missed/Blocked"])
    o["MiscFoul"] = 0
    o["MiscStuckBall"] = bconv(d["Stuck"])
    o["MiscDeadBot"] = bconv(d["Dead"])
    o["LowGoal"] = iconv(d["Low"])
    o["Pickups"] = iconv(d["Pickup"])
    o["EtcMobility"] = bconv(d["A Mobility"])
    o["EtcNoShow"] = bconv(d["No Show"])
    o["EtcDefending"] = bconv(d["Played D"])
    o["EtcDefended"] = bconv(d["Had D"])
    if d["A Start Pos"] == "Goalie":
        o["EtcGoalie"] = 1
    else:
        o["EtcGoalie"] = 0
    o["EtcGoalieBlock"] = bconv(d["A Goalie Block"])

    return md, [o[f] for f in fields]

def writetsv(filename, header, data):
    with open(filename, "w") as f:
        f.write("\t".join(header) + "\n")
        for line in data:
            f.write("\t".join(map(str, line)) + "\n")

header, data = readcsv("aerialassist.csv")
practice = []
qualification = []
elimination = []
for x in data:
    mode, line = convert(x, header)
    if mode == "Practice":
        practice.append(line)
    if mode == "Qualification":
        qualification.append(line)
    if mode == "Elimination":
        elimination.append(line)
writetsv("practice.tsv",fields, practice)
writetsv("qualification.tsv",fields, qualification)
writetsv("elimination.tsv",fields, elimination)





