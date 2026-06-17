class MyClass {
  final String name;
  final String region;
  final String country;
  final String tzId;
  final int localtimeEpoch;
  final Map<String, dynamic> location;
  final String localtime;

  MyClass({
    required this.name,
    required this.region,
    required this.country,
    required this.tzId,
    required this.localtimeEpoch,
    required this.location,
    required this.localtime,
  });

  MyClass copyWith({
    String? name,
    String? region,
    String? country,
    String? tzId,
    int? localtimeEpoch,
    Map<String, dynamic>? location,
    String? localtime
  }) {
    return MyClass(
      name: name ?? this.name,
      region: region ?? this.region,
      country: country ?? this.country,
      tzId: tzId ?? this.tzId,
      localtimeEpoch: localtimeEpoch ?? this.localtimeEpoch,
      location: location ?? this.location,
      localtime: localtime ?? this.localtime,
    );
  }
  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'region': region,
      'country': country,
      'tzId': tzId,
      'localtimeEpoch': localtimeEpoch,
      'location': location,
      'localtime': localtime,
    };
  }
  factory MyClass.fromJson(Map<String, dynamic> json) {
    return MyClass(
      name: json['name'],
      region: json['region'],
      country: json['country'],
      tzId: json['tzId'],
      localtimeEpoch: json['localtimeEpoch'],
      location: json['location'],
      localtime: json['localtime'],
    );
  }
  @override
  String toString() {
    return '''
      MyClass(
      name: $name,
      region: $region,
      country: $country,
      tzId: $tzId,
      localtimeEpoch: $localtimeEpoch,
      location: $location,
      localtime: $localtime,
      )
    ''';
  }
}
