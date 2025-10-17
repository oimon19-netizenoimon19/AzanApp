
import 'dart:io';
import 'package:adhan_dart/adhan_dart.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:shared_preferences/shared_preferences.dart';

const _channel = MethodChannel('com.abdulwahhab.azan/channel');

enum PrayerId { fajr, dhuhr, asr, maghrib, isha }

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const App());
}

class App extends StatelessWidget {
  const App({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Azan',
      theme: ThemeData(useMaterial3: true),
      home: const SettingsPage(),
    );
  }
}

class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key});
  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  final _fmt = DateFormat('hh:mm a');
  Map<PrayerId,int> offsets = { for (var p in PrayerId.values) p: 0 };
  Map<PrayerId,String?> files = { for (var p in PrayerId.values) p: null };
  late PrayerTimes pt;

  @override
  void initState() {
    super.initState();
    _load();
    _calcToday();
  }

  void _calcToday() {
    final coords = Coordinates(30.6682, 73.1114); // Sahiwal
    final params = CalculationMethod.Karachi()..madhab = Madhab.Hanafi;
    final date = DateTime.now();
    pt = PrayerTimes(coords, date, params);
  }

  Future<void> _load() async {
    final sp = await SharedPreferences.getInstance();
    for (var p in PrayerId.values) {
      files[p] = sp.getString('file_${p.name}');
      offsets[p] = sp.getInt('off_${p.name}') ?? 0;
    }
    setState((){});
  }

  Future<void> _save() async {
    final sp = await SharedPreferences.getInstance();
    for (var p in PrayerId.values) {
      final path = files[p];
      if (path != null) await sp.setString('file_${p.name}', path);
      await sp.setInt('off_${p.name}', offsets[p]!);
    }
  }

  String _label(PrayerId p) => p.name[0].toUpperCase() + p.name.substring(1);

  DateTime _time(PrayerId p) {
    switch(p) {
      case PrayerId.fajr: return pt.fajr!;
      case PrayerId.dhuhr: return pt.dhuhr!;
      case PrayerId.asr: return pt.asr!;
      case PrayerId.maghrib: return pt.maghrib!;
      case PrayerId.isha: return pt.isha!;
    }
  }

  Future<void> _pick(PrayerId p) async {
    final res = await FilePicker.platform.pickFiles(type: FileType.audio);
    if (res == null) return;
    files[p] = res.files.single.path;
    setState((){});
  }

  Future<void> _apply() async {
    await _save();
    final times = <String,int>{};
    for (var p in PrayerId.values) {
      final base = _time(p);
      final adj = base.add(Duration(minutes: offsets[p] ?? 0));
      times[p.name] = adj.millisecondsSinceEpoch;
    }
    final paths = <String,String>{};
    for (var p in PrayerId.values) {
      final v = files[p];
      if (v != null) paths[p.name] = v;
    }
    await _channel.invokeMethod('scheduleWithTimes', {
      'times': times,
      'files': paths,
    });
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Scheduled for today')));
  }

  Future<void> _play(PrayerId p) async {
    await _channel.invokeMethod('playNow', {'prayer': p.name});
  }

  @override
  Widget build(BuildContext context) {
    final items = [PrayerId.fajr,PrayerId.dhuhr,PrayerId.asr,PrayerId.maghrib,PrayerId.isha];
    return Scaffold(
      appBar: AppBar(title: const Text('Azan (Sahiwal • Karachi • Hanafi)')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const Text('Pick audio for each prayer, test, then Apply & Schedule.'),
          const SizedBox(height: 12),
          for (final p in items) Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Row(
                children: [
                  Expanded(child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('${_label(p)}  •  ${_fmt.format(_time(p))}'),
                      const SizedBox(height: 4),
                      Text(files[p] ?? 'No audio selected', style: const TextStyle(fontSize: 12, color: Colors.grey), overflow: TextOverflow.ellipsis),
                      const SizedBox(height: 6),
                      Row(children: [
                        const Text('Offset (min): '),
                        SizedBox(width: 60, child: TextFormField(
                          initialValue: (offsets[p]??0).toString(),
                          keyboardType: TextInputType.number,
                          onChanged: (v){ offsets[p] = int.tryParse(v) ?? 0; },
                        ))
                      ])
                    ],
                  )),
                  Column(children: [
                    ElevatedButton(onPressed: ()=>_pick(p), child: const Text('Pick')),
                    const SizedBox(height: 6),
                    ElevatedButton(onPressed: ()=>_play(p), child: const Text('Play')),
                  ])
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          FilledButton(onPressed: _apply, child: const Text('Apply & Schedule')),
        ],
      ),
    );
  }
}
