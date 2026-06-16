import 'dart:convert';

enum UserStatus { active, banned }

class LudoUser {
  final String id;
  final String name;
  final String email;
  final String avatarColor;
  int coins;
  int gems;
  UserStatus status;
  final bool isVip;
  final String vipFrame;
  final String vipDice;
  final int matchesPlayCount;
  final double winRatio;
  final DateTime registrationDate;

  LudoUser({
    required this.id,
    required this.name,
    required this.email,
    required this.avatarColor,
    required this.coins,
    required this.gems,
    required this.status,
    required this.isVip,
    required this.vipFrame,
    required this.vipDice,
    required this.matchesPlayCount,
    required this.winRatio,
    required this.registrationDate,
  });

  LudoUser copyWith({
    String? id,
    String? name,
    String? email,
    String? avatarColor,
    int? coins,
    int? gems,
    UserStatus? status,
    bool? isVip,
    String? vipFrame,
    String? vipDice,
    int? matchesPlayCount,
    double? winRatio,
    DateTime? registrationDate,
  }) {
    return LudoUser(
      id: id ?? this.id,
      name: name ?? this.name,
      email: email ?? this.email,
      avatarColor: avatarColor ?? this.avatarColor,
      coins: coins ?? this.coins,
      gems: gems ?? this.gems,
      status: status ?? this.status,
      isVip: isVip ?? this.isVip,
      vipFrame: vipFrame ?? this.vipFrame,
      vipDice: vipDice ?? this.vipDice,
      matchesPlayCount: matchesPlayCount ?? this.matchesPlayCount,
      winRatio: winRatio ?? this.winRatio,
      registrationDate: registrationDate ?? this.registrationDate,
    );
  }
}

enum TournamentStatus { open, ongoing, finished }

class Tournament {
  final String id;
  final String title;
  final int prizePool;
  final int entryFee;
  int registeredCount;
  final int maxPlayers;
  final DateTime startTime;
  TournamentStatus status;
  final String winnerName;

  Tournament({
    required this.id,
    required this.title,
    required this.prizePool,
    required this.entryFee,
    required this.registeredCount,
    required this.maxPlayers,
    required this.startTime,
    required this.status,
    this.winnerName = "",
  });

  Tournament copyWith({
    String? id,
    String? title,
    int? prizePool,
    int? entryFee,
    int? registeredCount,
    int? maxPlayers,
    DateTime? startTime,
    TournamentStatus? status,
    String? winnerName,
  }) {
    return Tournament(
      id: id ?? this.id,
      title: title ?? this.title,
      prizePool: prizePool ?? this.prizePool,
      entryFee: entryFee ?? this.entryFee,
      registeredCount: registeredCount ?? this.registeredCount,
      maxPlayers: maxPlayers ?? this.maxPlayers,
      startTime: startTime ?? this.startTime,
      status: status ?? this.status,
      winnerName: winnerName ?? this.winnerName,
    );
  }
}

class AdminActivityLog {
  final String id;
  final String action;
  final String target;
  final String adminUser;
  final DateTime timestamp;
  final String details;

  AdminActivityLog({
    required this.id,
    required this.action,
    required this.target,
    required this.adminUser,
    required this.timestamp,
    required this.details,
  });
}
