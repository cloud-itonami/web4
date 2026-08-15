# Operator quickstart

**This repository documents contracts that are live on Ethereum mainnet, owned by a
2-of-3 Safe.** `CLAUDE.md` lists a GCC token, a minter accepting ETH/USDC/USDT, the
Safe that owns both, and the deployer EOA. Nothing here should be run casually, and
this document deliberately contains no instructions for minting, transferring or
deploying anything. It answers two questions instead: what can be checked safely
from a checkout, and what is confusingly named.

Steps marked ✅ were run against this tree on 2026-08-16.

---

## 1. No key material is committed ✅

The first thing worth establishing about a repository that deploys contracts:

```bash
git ls-files | grep -E '\.env$|\.env\.|key|secret|mnemonic|\.pem$'
#   contracts/usdc-test-t0k3n8x/.env.example      <- the template only
# (the \.env\. alternative matters: without it, .env.example does not match
#  and the check reports nothing, which looks like the same clean answer)

git grep -cE '0x[0-9a-fA-F]{64}' -- .
#   (no output — zero matches anywhere in the tree)
```

**This repository is public** (`gh api repos/cloud-itonami/web4 --jq .private` →
`false`), so this is not a formality. A private key is 64 hex characters. **There is
not one 64-hex string in the repository**, which settles `.env.example` too without anyone needing to read its
values: a template that contained a real key would have matched. Its variables are
`MAINNET_RPC_URL`, `SEPOLIA_RPC_URL` and `DEPLOYER_PRIVATE_KEY`, and it comments
that the deployer is an "EOA that pays gas — NOT the Safe. Discard after role
transfer."

## 2. The addresses are internally consistent, and three are well-known ✅

```bash
grep -oE '0x[0-9a-fA-F]{40}' CLAUDE.md | sort -u
git grep -ohE '0x[0-9a-fA-F]{40}' -- . | sort | uniq -c | sort -rn
```

Every address `CLAUDE.md` documents appears once or twice in the tree and nowhere
does a second value compete with it. Three of the addresses in the code are
canonical mainnet constants rather than anything this project chose:

| address | what it is |
|---|---|
| `0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48` | USDC |
| `0xdAC17F958D2ee523a2206206994597C13D831ec7` | USDT |
| `0x5f4eC3Df9cbd43714FE2740f5E3616155c5b8419` | Chainlink ETH/USD aggregator |

That is consistent with `CLAUDE.md`'s claim that the minter accepts ETH, USDC and
USDT and prices ETH through Chainlink. (Those three are stated from general
knowledge of the mainnet deployments, not verified against a chain from here — this
document makes no network calls.)

## 3. ⚠ The mainnet token's source file is named `TestUSDC.sol`

`CLAUDE.md` names the contracts directory and the token's address and never says
which file implements it. Searching for the obvious name finds only the minter:

```bash
grep -oE '^contract [A-Za-z0-9_]+' contracts/usdc-test-t0k3n8x/src/*.sol
#   src/GCCMinter.sol:contract GCCMinter
#   src/TestUSDC.sol:contract TestUSDC
```

The token is `TestUSDC`, and its own docstring says so:

> `@title TestUSDC`
> `@notice Etzhayyim Computing Credits (GCC) — FiatTokenV2_2 ABI-compatible token.`

So the contract at the documented GCC Token address is implemented by a file called
`TestUSDC.sol`, declaring `contract TestUSDC`, inside a directory called
`usdc-test-t0k3n8x`. Every one of those three names reads as a test fixture. Anyone
auditing this — or grepping for `GCC` — will not find the token.

**And the docstring reasons about a testnet.** Among its stated differences from
Circle's production FiatTokenV2_2:

> - Single non-upgradeable deployment (no proxy)
> - `initialize` is called in the constructor
> - Balance/blacklist are NOT bit-packed (**clarity > gas savings on testnet**)

The first two are substantive and worth knowing: this token cannot be upgraded,
where Circle's is proxied. The third describes a testnet trade-off in a contract
`CLAUDE.md` places on Ethereum mainnet. Unpacked storage on mainnet is not a bug,
it just costs more gas — but the comment's premise and the deployment do not
match, and that is the kind of mismatch worth resolving before someone relies on
either.

Renaming a deployed contract's source is not a thing to do lightly, since the
address is fixed and the name is in the ABI. Whether the fix is a rename, a comment,
or a line in `CLAUDE.md` mapping address to file is the owner's call. The document
records that the mapping is currently written nowhere.

## 4. The Solidity tests ⚠ NOT WALKED, with the reason

There are two test suites — `test/GCCMinter.t.sol` (9.7 KB) and
`test/TestUSDC.t.sol` (22.0 KB) — against 32.5 KB of source, and **they do not fork
a chain**:

```bash
grep -cE 'createFork|createSelectFork|fork-url' test/*.sol foundry.toml
#   all zero
```

So `forge test` is a local-EVM run needing no RPC and no key. `forge` 1.7.1 is
installed. Two things are missing and one was blocked:

- `foundry.toml` sets `libs = ["lib"]` and **`lib/` does not exist**, so
  `forge-std/Test.sol` cannot resolve. `forge install foundry-rs/forge-std` fetches
  it.
- solc 0.8.24 is not in the local svm cache: `forge test --offline` fails with
  `can't install missing solc 0.8.24 in offline mode`.
- attempting the install through the workspace's resource governor was refused
  because another session held the build slot:
  `resource-guard: build is already running (pid=2786, repo=.../cloud-murakumo)`.

So the suites are not claimed to pass. To run them, when the slot is free:

```bash
cd contracts/usdc-test-t0k3n8x
node <root>/scripts/resource-guard.mjs run build -- forge install foundry-rs/forge-std --no-git
node <root>/scripts/resource-guard.mjs run build -- forge test
```

This is the most valuable unrun check in the repository: a minter that prices ETH
through an oracle and mints a token has arithmetic worth exercising, and its own
tests do it without touching a chain.

## 5. What the maturity instrument cannot see here ✅

The loop's tick reports it, and it is worth repeating where it applies:

```
· orgs/cloud-itonami/web4  own=0.043
    axis-ingest は 0bp だが計数外のファイルに URL が 32 件在る
```

45 tracked files including Solidity source, two test suites, a BPMN, an appview and
a Safe deployment runbook, scored 0.043 — the citation counter looks only under
`facts`/`catalog`/`data`, and the README component reads only `README.md` while this
repository has `README.edn`. Blind spots recorded in ADR-2608052000, not absences.

## 6. Two names in here point somewhere else ✅

Neither is broken; both cost a reader a minute.

`migration.edn` records where this code came from and where it went:

```bash
grep -o '"etzhayyim/[^"]*"' migration.edn
#   "etzhayyim/root"
#   "etzhayyim/com-etzhayyim-app-web4"     <- destination
```

The destination is not where you are. The remote is `cloud-itonami/web4`, and the
west entry is `name: web4 / remote: cloud-itonami`. Both names resolve to the same
repository — GitHub redirects the old one:

```bash
gh api repos/etzhayyim/com-etzhayyim-app-web4 --jq .full_name
#   cloud-itonami/web4
```

So `migration.edn` holds the name at migration time, which the workspace pins
deliberately (a repository is not renamed because its home moved). It is worth
knowing before you conclude the destination repo is missing — that exact confusion
cost this workspace a wrong answer once before, when `kagami`'s old name resolved
through a redirect that had no checkout behind it.

The second is §3 above: the token's address maps to a file named `TestUSDC.sol`.
