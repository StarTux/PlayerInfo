package com.winthier.playerinfo;

import com.cavetale.core.command.CommandWarn;
import com.winthier.playercache.PlayerCache;
import com.winthier.playerinfo.util.Players;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PlayerInfoCommands {
    private final PlayerInfo info;

    private boolean loginfoPrivate(UUID sender, String[] args) {
        if (args.length == 0) {
            if (sender == null) return false;
            info.getActions().loginfo(sender, sender);
        } else if (args.length == 1) {
            if (!info.hasPermission(sender, "playerinfo.loginfo.admin")) return false;
            final String playerArg = args[0];
            info.getActions().loginfo(sender, Players.getUuid(playerArg));
        } else {
            return false;
        }
        return true;
    }

    private boolean ontimePrivate(UUID sender, String[] args) {
        if (args.length == 0) {
            info.getActions().ontime(sender, sender);
        } else if (args.length <= 2) {
            String firstArg = args[0];
            if (firstArg.equalsIgnoreCase("-top")) {
                if (!info.hasPermission(sender, "playerinfo.ontime.top")) return false;
                int page = 0;
                if (args.length >= 2) {
                    String pageArg = args[1];
                    try {
                        page = Integer.parseInt(pageArg) - 1;
                    } catch (NumberFormatException nfe) {
                        page = -1;
                    }
                    if (page < 0) throw new PlayerInfoException("Page number expected, got: " + pageArg);
                }
                info.getActions().rankOntimes(sender, page);
            } else {
                if (!info.hasPermission(sender, "playerinfo.ontime.admin")) return false;
                if (args.length != 1) return false;
                info.getActions().ontime(sender, Players.getUuid(firstArg));
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean playerinfoIgnoredIPs(UUID sender, String[] args) {
        if (args.length == 0) return false;
        String firstArg = args[0];
        if (args.length == 1 && firstArg.equalsIgnoreCase("List")) {
            info.getActions().listIgnoredIPs(sender);
        } else if (args.length == 2 && firstArg.equalsIgnoreCase("Add")) {
            String ipArg = args[1];
            info.getActions().addIgnoredIP(sender, ipArg);
        } else if (args.length == 2 && firstArg.equalsIgnoreCase("Remove")) {
            String ipArg = args[1];
            info.getActions().removeIgnoredIP(sender, ipArg);
        }
        return true;
    }

    private boolean playerinfoListIPs(UUID sender, String[] args) {
        if (args.length != 1) return false;
        info.getActions().listPlayerIPs(sender, Players.getUuid(args[0]));
        return true;
    }

    private boolean playerinfoSharedIPs(UUID sender, String[] args) {
        if (args.length < 1) return false;
        info.getActions().findSharedIPs(sender, Players.getUuids(Arrays.<String>asList(args)));
        return true;
    }

    private boolean playerinfoAltIPs(UUID sender, String[] args) {
        if (args.length != 1) return false;
        UUID playerUuid = Players.getUuid(args[0]);
        info.getActions().listAltIPs(sender, playerUuid);
        return true;
    }

    private void playerinfoUsage(UUID sender) {
        info.send(sender, "&3/PlayerInfo &bIgnoredIPs List|Add|Remove &7- &3Ignored IPs");
        info.send(sender, "&3/PlayerInfo &bListIPs <&oName&b> &7- &3Show known IPs of player");
        info.send(sender, "&3/PlayerInfo &bSharedIPs <&oName&b...> &7- &3Find common IPs");
        info.send(sender, "&3/PlayerInfo &bAltIPs <&oName&b> &7- &3Find common IPs of alts");
        info.send(sender, "&3/PlayerInfo &bOnlineCountries &7- &3Where are online players from");
        info.send(sender, "&3/PlayerInfo &bmigrate <from> <to> &7- &3Migrate loginfo/ontime data");
    }

    private boolean playerinfoPrivate(UUID sender, String[] args) {
        if (args.length == 0) return false;
        String firstArg = args[0];
        if (args.length >= 2 && firstArg.equalsIgnoreCase("IgnoredIPs")) {
            return playerinfoIgnoredIPs(sender, Arrays.copyOfRange(args, 1, args.length));
        } else if (args.length == 2 && firstArg.equalsIgnoreCase("ListIPs")) {
            return playerinfoListIPs(sender, Arrays.copyOfRange(args, 1, args.length));
        } else if (args.length >= 2 && firstArg.equalsIgnoreCase("SharedIPs")) {
            return playerinfoSharedIPs(sender, Arrays.copyOfRange(args, 1, args.length));
        } else if (args.length >= 2 && firstArg.equalsIgnoreCase("AltIPs")) {
            return playerinfoAltIPs(sender, Arrays.copyOfRange(args, 1, args.length));
        } else if (args.length >= 1 && firstArg.equalsIgnoreCase("OnlineCountries")) {
            info.getActions().onlineCountries(sender);
            return true;
        } else if (args.length == 3 && firstArg.equalsIgnoreCase("migrate")) {
            try {
                info.getActions().migrate(sender, PlayerCache.require(args[1]), PlayerCache.require(args[2]));
            } catch (CommandWarn warn) {
                info.send(sender, "%c%s", warn.getMessage());
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean playerinfo(UUID sender, String[] args) {
        boolean result = true;
        try {
            result = playerinfoPrivate(sender, args);
        } catch (PlayerInfoException pie) {
            info.send(sender, "&c%s", pie.getMessage());
        }
        if (!result) playerinfoUsage(sender);
        return true;
    }

    public boolean status(UUID sender, String[] args) {
        try {
            if (args.length == 1) {
                String playerArg = args[0];
                info.getActions().status(sender, Players.getUuid(playerArg));
            } else {
                return false;
            }
        } catch (PlayerInfoException pie) {
            info.send(sender, "&c%s", pie.getMessage());
        }
        return true;
    }

    public boolean loginfo(UUID sender, String[] args) {
        boolean result = false;
        try {
            result = loginfoPrivate(sender, args);
        } catch (PlayerInfoException pie) {
            info.send(sender, "&c%s", pie.getMessage());
            return true;
        }
        if (!result) {
            info.send(sender, "&b/LogInfo &7-&3 Get your login info");
            if (info.hasPermission(sender, "playerinfo.loginfo.admin")) {
                info.send(sender, "&b/LogInfo <&oName&b> &7-&3 Get a player's login info");
            }
        }
        return true;
    }

    public boolean firstlog(UUID sender, String[] args) {
        if (args.length > 1) return false;
        final int page;
        if (args.length >= 1) {
            String pageArg = args[0];
            try {
                page = Integer.parseInt(pageArg) - 1;
            } catch (NumberFormatException nfe) {
                throw new PlayerInfoException("Invalid page number: " + pageArg);
            }
            if (page < 0) throw new PlayerInfoException("Illegal page number: " + page);
        } else {
            page = 0;
        }
        info.getActions().firstlog(sender, page);
        return true;
    }

    public boolean lastlog(UUID sender, String[] args) {
        if (args.length > 1) return false;
        final int page;
        if (args.length >= 1) {
            String pageArg = args[0];
            try {
                page = Integer.parseInt(pageArg) - 1;
            } catch (NumberFormatException nfe) {
                throw new PlayerInfoException("Invalid page number: " + pageArg);
            }
            if (page < 0) throw new PlayerInfoException("Illegal page number: " + page);
        } else {
            page = 0;
        }
        info.getActions().lastlog(sender, page);
        return true;
    }

    public boolean ontime(UUID sender, String[] args) {
        boolean result = false;
        try {
            result = ontimePrivate(sender, args);
        } catch (PlayerInfoException pie) {
            info.send(sender, "&c%s", pie.getMessage());
        }
        if (!result) {
            info.send(sender, "&b/OnTime &7-&3 Get your ontime");
            if (info.hasPermission(sender, "playerinfo.ontime.admin")) {
                info.send(sender, "&b/OnTime <&oName&b> &7-&3 Get a player's ontime");
            }
            if (info.hasPermission(sender, "playerinfo.ontime.top")) {
                info.send(sender, "&b/OnTime -Top <&oPage&b> &7-&3 Rank online time");
            }
        }
        return true;
    }
}
