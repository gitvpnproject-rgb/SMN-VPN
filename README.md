# SMN VPN — Secure Mesh Network VPN

> Decentralized mesh network VPN for Android. No central server. Multi-hop routing. AES-256 encryption.

![Platform](https://img.shields.io/badge/Platform-Android-green?style=flat-square)
![Language](https://img.shields.io/badge/Language-Kotlin-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)
![Status](https://img.shields.io/badge/Status-In%20Development-orange?style=flat-square)

---

## What is SMN VPN?

SMN VPN is not just a VPN — it's a decentralized mesh network where:

- ✅ No central server — the network keeps working if individual nodes go down
- ✅ Anyone can connect their own server or device as a node
- ✅ Traffic passes through multiple nodes (multi-hop routing)
- ✅ Strong encryption — AES-256, AES-128, ChaCha20
- ✅ Open source — anyone can use, improve, or run their own node

```
Your Device
     ↓
  Node A 🇩🇪
     ↓
  Node B 🇳🇱
     ↓
  Node C 🇺🇸
     ↓
  Internet 🌍
```

---

## How it works

Each device connects to **3 nodes simultaneously**.  
If one node goes down — traffic instantly switches to the next.  
No interruptions. No downtime.

```
Device ──── Node 1 (active)
       ──── Node 2 (standby)
       ──── Node 3 (standby)
```

Weak nodes don't get removed — they just receive less traffic.  
Dead nodes get replaced automatically from the network.

---

## Features

### 🟢 Standard (for everyone)
| Feature | Description |
|---|---|
| Kill Switch | Blocks internet if VPN disconnects |
| DNS Protection | Prevents DNS query leaks |
| IPv6 Protection | Blocks IPv6 leaks |
| Auto-reconnect | Restores VPN automatically |
| Smart server selection | Always picks the fastest node |

### 🔴 Paranoid (advanced users)
| Feature | Description |
|---|---|
| Multi-hop VPN | Routes traffic through 2–3 servers |
| Smart routing | Different traffic takes different paths |
| Key rotation | Encryption keys change every 10 minutes |
| Traffic obfuscation | Hides VPN from DPI inspection |
| High anonymity mode | Enhanced network-level routing |

---

## Encryption

| Mode | Algorithm | Speed | Security |
|---|---|---|---|
| 🔴 Maximum | AES-256-GCM | Medium | Highest |
| 🟡 Balanced | AES-128-GCM | Fast | Good |
| 🟢 Fast | ChaCha20 | Fastest | Excellent on mobile |

> AES-GCM provides both encryption and integrity check in one algorithm.  
> ChaCha20 is used by WireGuard — faster than AES on mobile CPUs.

---

## Node Types

**Server node**  
A VPS or dedicated server with a good connection.  
Connect by entering IP + password.

**Device node**  
A phone, tablet, or PC.  
One button to join the network and share bandwidth.  
Like Tor — your device helps forward traffic for others.

---

## Connecting a Node

**Via link:**
```
smn://join/<token>
```

**Via QR code** — scan to add instantly.

**Manual:**  
Settings → Add node → Enter IP + password

---

## Project Structure

```
SMN-VPN/
├── app/src/main/
│   ├── java/com/smn/vpn/
│   │   ├── MainActivity.kt           ← main screen
│   │   ├── service/
│   │   │   ├── SMNVpnService.kt      ← VPN tunnel
│   │   │   ├── SMNNodeManager.kt     ← 3-node failover system
│   │   │   ├── SMNNode.kt            ← single node connection
│   │   │   └── SMNCrypto.kt          ← AES-256 / AES-128 / ChaCha20
│   │   ├── ui/
│   │   │   └── SettingsActivity.kt   ← Standard + Paranoid profiles
│   │   └── receiver/
│   │       └── BootReceiver.kt       ← auto-start on boot
│   └── res/
│       └── layout/
│           ├── activity_main.xml
│           └── activity_settings.xml
├── .github/workflows/
│   └── build.yml                     ← auto-build APK on push
└── README.md
```

---

## Build APK

APK is built automatically via GitHub Actions on every push.

1. Go to the **Actions** tab
2. Wait for the build to finish (~5 min)
3. Download `SMN-VPN-debug.apk` from **Artifacts**

---

## Running Your Own Node

You can run an SMN node on any Linux VPS.

**Free VPS options:**
| Provider | Region | Free tier |
|---|---|---|
| Oracle Cloud | USA / EU / Asia | Forever free (2 servers) |
| Google Cloud | 30+ countries | $300 for 90 days |
| Fly.io | 30+ countries | Free tier |

**Setup instructions:** coming soon in `/docs/node-setup.md`

---

## Philosophy

> The network should survive without its creator.

- Open source from day one
- No logs, no tracking
- No central point of failure
- Anyone can run a node
- Community-driven development

---

## Honest disclaimer

SMN VPN **increases your privacy** by hiding your IP and encrypting traffic.  
It does **not** guarantee 100% anonymity.  
No VPN does.

---

## License

MIT License — free to use, modify, and distribute.  
See [LICENSE](LICENSE) for details.

---

## Status

🚧 **Active development** — core VPN engine is being built.

- [x] Architecture design
- [x] Encryption module (AES-256 / AES-128 / ChaCha20)
- [x] Node manager with 3-node failover
- [x] Settings (Standard + Paranoid profiles)
- [ ] Kill Switch implementation
- [ ] Node server code
- [ ] First working APK
- [ ] DHT node discovery
- [ ] Traffic obfuscation

---

*SMN VPN — Secure Mesh Network*
