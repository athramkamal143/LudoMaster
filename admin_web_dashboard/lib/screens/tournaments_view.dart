import 'package:flutter/material.dart';
import '../models/admin_models.dart';
import '../services/mock_api_service.dart';

class TournamentsView extends StatefulWidget {
  const TournamentsView({Key? key}) : super(key: key);

  @override
  State<TournamentsView> createState() => _TournamentsViewState();
}

class _TournamentsViewState extends State<TournamentsView> {
  final MockApiService _apiService = MockApiService();

  // New Tournament parameters form state
  final _formKey = GlobalKey<FormState>();
  String _title = "";
  int _prizePool = 100000;
  int _entryFee = 5000;
  int _maxPlayers = 128;
  int _startDelayHours = 12;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: LayoutBuilder(
        builder: (context, constraints) {
          final bool isWide = constraints.maxWidth > 960;
          return Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Left: Core Grid List of existing tournaments
              Expanded(
                flex: 3,
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'GLOBAL CHAMPIONSHIP TOURNAMENTS',
                          style: TextStyle(fontWeight: FontWeight.black, color: Colors.white, fontSize: 14),
                        ),
                        const SizedBox(height: 16),
                        Expanded(
                          child: ListenableBuilder(
                            listenable: _apiService,
                            builder: (context, _) {
                              final List<Tournament> activeT = _apiService.tournaments;
                              if (activeT.isEmpty) {
                                return const Center(child: Text("No tournament lobbies scheduling. Initiate one on the panel!"));
                              }
                              return GridView.builder(
                                gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                                  crossAxisCount: constraints.maxWidth > 1200 ? 2 : 1,
                                  crossAxisSpacing: 16,
                                  mainAxisSpacing: 16,
                                  childAspectRatio: 1.8,
                                ),
                                itemCount: activeT.length,
                                itemBuilder: (context, index) {
                                  final t = activeT[index];
                                  return _buildTournamentCard(t);
                                },
                              );
                            },
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),

              if (isWide) ...[
                const SizedBox(width: 24),
                // Right: Sidebar builder form panel for quick deployment
                Expanded(
                  flex: 2,
                  child: _buildInitiatorFormPanel(),
                ),
              ],
            ],
          );
        },
      ),
    );
  }

  Widget _buildTournamentCard(Tournament t) {
    final double registrationRatio = t.maxPlayers > 0 ? (t.registeredCount / t.maxPlayers) : 0;
    Color statusColor = const Color(0xFFCA8A04);
    if (t.status == TournamentStatus.ongoing) {
      statusColor = Colors.blue;
    } else if (t.status == TournamentStatus.finished) {
      statusColor = Colors.green;
    }

    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF0C1322),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: const Color(0xFF1F2B44)),
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: statusColor.withOpacity(0.08),
                  borderRadius: BorderRadius.circular(6),
                  border: Border.all(color: statusColor.withOpacity(0.18)),
                ),
                child: Text(
                  t.status.toString().split('.').last.toUpperCase(),
                  style: TextStyle(color: statusColor, fontSize: 9, fontWeight: FontWeight.black),
                ),
              ),
              IconButton(
                onPressed: () {
                  _apiService.deleteTournament(t.id);
                },
                icon: const Icon(Icons.delete_outline, color: Colors.white24, size: 18),
              ),
            ],
          ),
          const SizedBox(height: 8),

          Text(
            t.title,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(fontWeight: FontWeight.black, fontSize: 15, color: Colors.white),
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              Text('ID: ${t.id}  • ', style: const TextStyle(color: Colors.white38, fontSize: 11)),
              Text('Entry: 🪙 ${_formatCompact(t.entryFee)}', style: const TextStyle(color: Color(0xFFCA8A04), fontSize: 11, fontWeight: FontWeight.bold)),
            ],
          ),

          const SizedBox(height: 12),
          // Prize Pool Indicator Box
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: const Color(0xFF131D31),
              borderRadius: BorderRadius.circular(6),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('PRIZE POOL PAYOUT:', style: TextStyle(color: Colors.white30, fontSize: 10, fontWeight: FontWeight.bold)),
                Text('🪙 ${_formatCompact(t.prizePool)} COINS', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.black, fontSize: 12)),
              ],
            ),
          ),

          const SizedBox(height: 12),
          // Registration Progress bar
          if (t.status != TournamentStatus.finished) ...[
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Registration Progress: ${t.registeredCount}/${t.maxPlayers}', style: const TextStyle(color: Colors.white38, fontSize: 11)),
                Text('${(registrationRatio * 100).toStringAsFixed(0)}%', style: const TextStyle(color: Colors.white60, fontSize: 11, fontWeight: FontWeight.bold)),
              ],
            ),
            const SizedBox(height: 6),
            ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: LinearProgressIndicator(
                value: registrationRatio,
                backgroundColor: const Color(0xFF131D31),
                color: statusColor,
                minHeight: 5,
              ),
            ),
          ] else ...[
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('TOURNAMENT WINNER:', style: TextStyle(color: Colors.green, fontWeight: FontWeight.bold, fontSize: 11)),
                Text('👑 ${t.winnerName}', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 11)),
              ],
            ),
          ],
        ],
      ),
    );
  }

  // Right builder panels
  Widget _buildInitiatorFormPanel() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Row(
                children: [
                  Icon(Icons.emoji_events, color: Color(0xFFCA8A04), size: 20),
                  SizedBox(width: 10),
                  Text(
                    'INITIATE CHAMPIONSHIP',
                    style: TextStyle(fontWeight: FontWeight.black, color: Colors.white, fontSize: 14),
                  ),
                ],
              ),
              const SizedBox(height: 6),
              const Text(
                'Register a brand new public tournament across active matchmaking servers.',
                style: TextStyle(fontSize: 11, color: Colors.white38),
              ),
              const Divider(),
              const SizedBox(height: 12),

              // Title input
              const Text('Championship Branding', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.white60)),
              const SizedBox(height: 6),
              TextFormField(
                style: const TextStyle(fontSize: 13),
                decoration: const InputDecoration(
                  hintText: 'e.g. Master Golden Gladiator Clash',
                  hintStyle: TextStyle(color: Colors.white24),
                ),
                validator: (val) {
                  if (val == null || val.isEmpty) {
                    return "Branding name is required.";
                  }
                  return null;
                },
                onSaved: (val) => _title = val ?? "",
              ),
              const SizedBox(height: 16),

              // Prize Pool & Admission Fee
              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Total Prize Pool (Coins)', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.white60)),
                        const SizedBox(height: 6),
                        TextFormField(
                          initialValue: "100000",
                          style: const TextStyle(fontSize: 13),
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(),
                          onSaved: (val) => _prizePool = int.tryParse(val ?? "100000") ?? 100000,
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Admission Entry Fee', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.white60)),
                        const SizedBox(height: 6),
                        TextFormField(
                          initialValue: "5000",
                          style: const TextStyle(fontSize: 13),
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(),
                          onSaved: (val) => _entryFee = int.tryParse(val ?? "5000") ?? 5000,
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),

              // Players amount
              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Max Player Slot Limits', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.white60)),
                        const SizedBox(height: 6),
                        DropdownButtonFormField<int>(
                          value: _maxPlayers,
                          dropdownColor: const Color(0xFF131D31),
                          style: const TextStyle(fontSize: 13, color: Colors.white),
                          decoration: const InputDecoration(contentPadding: EdgeInsets.symmetric(horizontal: 10)),
                          items: const [
                            DropdownMenuItem(value: 32, child: Text("32 Slots")),
                            DropdownMenuItem(value: 64, child: Text("64 Slots")),
                            DropdownMenuItem(value: 128, child: Text("128 Slots")),
                            DropdownMenuItem(value: 256, child: Text("256 Slots")),
                          ],
                          onChanged: (val) {
                            if (val != null) {
                              setState(() {
                                _maxPlayers = val;
                              });
                            }
                          },
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Trigger Delay (Hours)', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.white60)),
                        const SizedBox(height: 6),
                        TextFormField(
                          initialValue: "12",
                          style: const TextStyle(fontSize: 13),
                          keyboardType: TextInputType.number,
                          decoration: const InputDecoration(),
                          onSaved: (val) => _startDelayHours = int.tryParse(val ?? "12") ?? 12,
                        ),
                      ],
                    ),
                  ),
                ],
              ),

              const Spacer(),

              // Submit button
              ElevatedButton(
                onPressed: () {
                  if (_formKey.currentState?.validate() ?? false) {
                    _formKey.currentState?.save();
                    _apiService.createTournament(
                      title: _title,
                      prizePool: _prizePool,
                      entryFee: _entryFee,
                      maxPlayers: _maxPlayers,
                      startTime: DateTime.now().add(Duration(hours: _startDelayHours)),
                    );
                    _formKey.currentState?.reset();
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Championship deployed to node queue!')),
                    );
                  }
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFFCA8A04),
                  foregroundColor: Colors.black,
                  minimumSize: const Size.fromHeight(48),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                ),
                child: const Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.flash_on, size: 18),
                    SizedBox(width: 8),
                    Text('DEPLOY TO LIVE NETWORKS', style: TextStyle(fontWeight: FontWeight.black, fontSize: 13)),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _formatCompact(int value) {
    if (value >= 1000000) {
      return "${(value / 1000000).toStringAsFixed(1)}M";
    } else if (value >= 1000) {
      return "${(value / 1000).toStringAsFixed(1)}K";
    }
    return value.toString();
  }
}
