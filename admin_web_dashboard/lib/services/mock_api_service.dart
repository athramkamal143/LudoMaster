import 'dart:async';
import 'package:flutter/foundation.dart';
import '../models/admin_models.dart';

class MockApiService extends ChangeNotifier {
  // Singleton Pattern
  static final MockApiService _instance = MockApiService._internal();
  factory MockApiService() => _instance;
  MockApiService._internal() {
    _initData();
  }

  // --- Real-world mock databases ---
  final List<LudoUser> _users = [];
  final List<Tournament> _tournaments = [];
  final List<AdminActivityLog> _logs = [];

  List<LudoUser> get users => List.unmodifiable(_users);
  List<Tournament> get tournaments => List.unmodifiable(_tournaments);
  List<AdminActivityLog> get logs => List.unmodifiable(_logs);

  void _initData() {
    // Standard mock user base
    _users.addAll([
      LudoUser(
        id: "USR-0821",
        name: "Rahul Verma",
        email: "rahul.verma@example.com",
        avatarColor: "0xFF3B82F6", // Blue
        coins: 145200,
        gems: 850,
        status: UserStatus.active,
        isVip: true,
        vipFrame: "Golden Crown Gladiator",
        vipDice: "Emperor's Golden Dice",
        matchesPlayCount: 312,
        winRatio: 0.68,
        registrationDate: DateTime.now().subtract(const Duration(days: 45)),
      ),
      LudoUser(
        id: "USR-4412",
        name: "Priya Sharma",
        email: "priya.sharma@example.com",
        avatarColor: "0xFFEC4899", // Pink
        coins: 82100,
        gems: 320,
        status: UserStatus.active,
        isVip: true,
        vipFrame: "Elite Platinum Sovereign",
        vipDice: "Cosmic Vortex Dice",
        matchesPlayCount: 184,
        winRatio: 0.54,
        registrationDate: DateTime.now().subtract(const Duration(days: 28)),
      ),
      LudoUser(
        id: "USR-9923",
        name: "Vijay Kumar",
        email: "vijay.k@example.com",
        avatarColor: "0xFF10B981", // Green
        coins: 11200,
        gems: 10,
        status: UserStatus.active,
        isVip: false,
        vipFrame: "None",
        vipDice: "Classic",
        matchesPlayCount: 62,
        winRatio: 0.42,
        registrationDate: DateTime.now().subtract(const Duration(days: 12)),
      ),
      LudoUser(
        id: "USR-1092",
        name: "Aniket Jaiswal",
        email: "aniket22@example.com",
        avatarColor: "0xFFF59E0B", // Amber
        coins: 450,
        gems: 0,
        status: UserStatus.banned,
        isVip: false,
        vipFrame: "None",
        vipDice: "Classic",
        matchesPlayCount: 9,
        winRatio: 0.11,
        registrationDate: DateTime.now().subtract(const Duration(days: 90)),
      ),
      LudoUser(
        id: "USR-5521",
        name: "Sam Wilson (Guest)",
        email: "guest_5521@ludo.net",
        avatarColor: "0xFF6B7280", // Gray
        coins: 18900,
        gems: 40,
        status: UserStatus.active,
        isVip: false,
        vipFrame: "None",
        vipDice: "Classic",
        matchesPlayCount: 42,
        winRatio: 0.48,
        registrationDate: DateTime.now().subtract(const Duration(days: 5)),
      ),
    ]);

    // Setup active and past tournaments
    _tournaments.addAll([
      Tournament(
        id: "TRN-ALPHA",
        title: "Royale Platinum Gladiators",
        prizePool: 500000,
        entryFee: 10000,
        registeredCount: 98,
        maxPlayers: 128,
        startTime: DateTime.now().add(const Duration(hours: 14)),
        status: TournamentStatus.open,
      ),
      Tournament(
        id: "TRN-BETA",
        title: "Neon Cosmic Dice Duel",
        prizePool: 150000,
        entryFee: 2500,
        registeredCount: 64,
        maxPlayers: 64,
        startTime: DateTime.now().add(const Duration(hours: 4)),
        status: TournamentStatus.ongoing,
      ),
      Tournament(
        id: "TRN-OMNIC",
        title: "Global Weekend Masters Cup",
        prizePool: 1200000,
        entryFee: 20000,
        registeredCount: 256,
        maxPlayers: 256,
        startTime: DateTime.now().subtract(const Duration(days: 2)),
        status: TournamentStatus.finished,
        winnerName: "Rahul Verma",
      ),
    ]);

    // Setup audit activity sequence list
    _logs.addAll([
      AdminActivityLog(
        id: "LOG-001",
        action: "INITIALIZATION",
        target: "Admin console",
        adminUser: "Athram Kamal",
        timestamp: DateTime.now().subtract(const Duration(hours: 3)),
        details: "Ludo Master Web Portal securely booted with full encryption catalogs.",
      ),
      AdminActivityLog(
        id: "LOG-002",
        action: "BAN USER",
        target: "USR-1092",
        adminUser: "Athram Kamal",
        timestamp: DateTime.now().subtract(const Duration(hours: 2)),
        details: "User banned due to severe token collision hacks and chat abuse logs.",
      )
    ]);
  }

  // --- API Action methods ---

  /// Adjust / Manage coins for a single user
  Future<bool> adjustUserCoins(String userId, int newCoinAmount) async {
    await Future.delayed(const Duration(milliseconds: 300));
    final idx = _users.indexWhere((u) => u.id == userId);
    if (idx != -1) {
      final oldCoins = _users[idx].coins;
      _users[idx].coins = newCoinAmount;
      
      // Log this action
      _addLog(
        action: "ADJUST COINS",
        target: userId,
        details: "Manually adjusted coins from $oldCoins 🪙 to $newCoinAmount 🪙 for ${_users[idx].name}",
      );
      
      notifyListeners();
      return true;
    }
    return false;
  }

  /// Adjust Gems for a user
  Future<bool> adjustUserGems(String userId, int newGemsAmount) async {
    await Future.delayed(const Duration(milliseconds: 250));
    final idx = _users.indexWhere((u) => u.id == userId);
    if (idx != -1) {
      final oldGems = _users[idx].gems;
      _users[idx].gems = newGemsAmount;

      _addLog(
        action: "ADJUST GEMS",
        target: userId,
        details: "Manually adjusted gems from $oldGems 💎 to $newGemsAmount 💎 for ${_users[idx].name}",
      );

      notifyListeners();
      return true;
    }
    return false;
  }

  /// Ban / Unban user toggles
  Future<bool> toggleUserBan(String userId) async {
    await Future.delayed(const Duration(milliseconds: 300));
    final idx = _users.indexWhere((u) => u.id == userId);
    if (idx != -1) {
      final oldStatus = _users[idx].status;
      final nextStatus = oldStatus == UserStatus.active ? UserStatus.banned : UserStatus.active;
      _users[idx].status = nextStatus;

      _addLog(
        action: nextStatus == UserStatus.banned ? "BAN USER" : "UNBAN USER",
        target: userId,
        details: "Account state forced to: ${nextStatus.toString().split('.').last.toUpperCase()} for ${_users[idx].name}",
      );

      notifyListeners();
      return true;
    }
    return false;
  }

  /// Add a brand-new tournament
  Future<bool> createTournament({
    required String title,
    required int prizePool,
    required int entryFee,
    required int maxPlayers,
    required DateTime startTime,
  }) async {
    await Future.delayed(const Duration(milliseconds: 400));
    final generatedId = "TRN-${title.replaceAll(' ', '').take(6).toUpperCase()}-${DateTime.now().millisecond}";
    final t = Tournament(
      id: generatedId,
      title: title,
      prizePool: prizePool,
      entryFee: entryFee,
      registeredCount: 0,
      maxPlayers: maxPlayers,
      startTime: startTime,
      status: TournamentStatus.open,
    );
    _tournaments.insert(0, t);

    _addLog(
      action: "CREATE TOURNAMENT",
      target: generatedId,
      details: "Created new tournament '$title' with $prizePool 🪙 prize pool.",
    );

    notifyListeners();
    return true;
  }

  /// Delete or stop tournament Campaign
  Future<bool> deleteTournament(String tournamentId) async {
    await Future.delayed(const Duration(milliseconds: 200));
    _tournaments.removeWhere((t) => t.id == tournamentId);

    _addLog(
      action: "DELETE TOURNAMENT",
      target: tournamentId,
      details: "Permanently destroyed tournament schedule mapping.",
    );

    notifyListeners();
    return true;
  }

  // Helper logger
  void _addLog({
    required String action,
    required String target,
    required String details,
  }) {
    _logs.insert(
      0,
      AdminActivityLog(
        id: "LOG-${DateTime.now().microsecondsSinceEpoch.toString().takeLast(4)}",
        action: action,
        target: target,
        adminUser: "Athram Kamal",
        timestamp: DateTime.now(),
        details: details,
      ),
    );
  }

  // --- Real-time statistics calculators ---
  int get totalUsersCount => _users.length;
  int get activeUsersCount => _users.where((u) => u.status == UserStatus.active).length;
  int get bannedUsersCount => _users.where((u) => u.status == UserStatus.banned).length;
  int get vipUsersCount => _users.where((u) => u.isVip).length;

  int get systemTotalCoinCirculation {
    return _users.fold(0, (sum, u) => sum + u.coins);
  }

  int get systemTotalGemCirculation {
    return _users.fold(0, (sum, u) => sum + u.gems);
  }
}

extension StringExtension on String {
  String take(int count) {
    if (length <= count) return this;
    return substring(0, count);
  }
  String takeLast(int count) {
    if (length <= count) return this;
    return substring(length - count);
  }
}
