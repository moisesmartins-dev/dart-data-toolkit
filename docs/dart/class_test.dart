/// Test file — covers all property types supported by Dart Data Toolkit.
/// Place your cursor inside a class and use the actions to generate methods.
///

void main() {
  // print(addressMock);
  print(userProfileMock);
}

// Nested custom class — use to test custom-class handling in toJson/fromJson/toString
class Address {
  final String street;
  final String city;
  final String? state;
  final int zipCode;

  const Address({
    required this.street,
    required this.city,
    required this.zipCode,
    this.state,
  });

  @override
  String toString() {
    return 'Address(\n'
        '  street: $street,\n'
        '  city: $city,\n'
        '  state: $state,\n'
        '  zipCode: $zipCode,\n'
        ')';
  }
}

// Main test class — exercises every supported type combination
class UserProfile {
  // Primitives — non-nullable
  final String name;
  final int age;
  final double score;
  final bool isActive;
  final num balance;

  // Primitives — nullable
  final String? nickname;
  final int? referralCount;
  final double? rating;
  final bool? isVerified;

  // Collections
  final List<String> tags;
  final List<int> scores;
  final Map<String, dynamic> metadata;
  final Map<String, int> stats;
  final Set<String> roles;

  // Nullable collections
  final List<String>? optionalTags;
  final Map<String, dynamic>? extraData;

  // Custom class — non-nullable and nullable
  final Address address;
  final Address? billingAddress;

  // Property with default value
  final String status;

  const UserProfile({
    required this.name,
    required this.age,
    required this.score,
    required this.isActive,
    required this.balance,
    this.nickname,
    this.referralCount,
    this.rating,
    this.isVerified,
    required this.tags,
    required this.scores,
    required this.metadata,
    required this.stats,
    required this.roles,
    this.optionalTags,
    this.extraData,
    required this.address,
    this.billingAddress,
    this.status = 'active',
  });



  @override
  String toString() {
    return 'UserProfile(\n'
        '  name: $name,\n'
        '  age: $age,\n'
        '  score: $score,\n'
        '  isActive: $isActive,\n'
        '  balance: $balance,\n'
        '  nickname: $nickname,\n'
        '  referralCount: $referralCount,\n'
        '  rating: $rating,\n'
        '  isVerified: $isVerified,\n'
        '  tags: $tags,\n'
        '  scores: $scores,\n'
        '  metadata: $metadata,\n'
        '  stats: $stats,\n'
        '  roles: $roles,\n'
        '  optionalTags: $optionalTags,\n'
        '  extraData: $extraData,\n'
        '  address: ${address.toString()},\n'
        '  billingAddress: ${billingAddress?.toString()},\n'
        '  status: $status,\n'
        ')';
  }
}

const addressMock = Address(
  street: '123 Main St',
  city: 'Springfield',
  zipCode: 12345,
  state: 'IL',
);

const userProfileMock =
UserProfile(
  name: 'Jane Doe',
  age: 28,
  score: 9.5,
  isActive: true,
  balance: 1500.0,
  nickname: 'jane',
  referralCount: 3,
  rating: 4.8,
  isVerified: true,
  tags: [
    'flutter',
    'dart',
    'kotlin',
  ],
  scores: [
    95,
    88,
    76,
  ],
  metadata: {
    'source': 'web',
    'version': 2,
  },
  stats: {
    'logins': 42,
    'posts': 7,
  },
  roles: {
    'admin',
    'user',
  },
  optionalTags: [
    'vip',
  ],
  extraData: {
    'theme': 'dark',
  },
  address: Address(
    street: '123 Main St',
    city: 'Springfield',
    zipCode: 12345,
    state: 'IL',
  ),
  billingAddress: Address(
    street: '123 Main St',
    city: 'Springfield',
    zipCode: 12345,
    state: 'IL',
  ),
  status: 'active',
);
