# Submission checklist

| # | Item | Where to get it | Done |
|---|---|---|---|
| 1 | Source code | GitHub repository (public, or shared with the reviewer) | [ ] |
| 2 | Live deployed URL | Render service URL, e.g. `https://dispatch-load-balancer.onrender.com` | [ ] |
| 3 | Swagger documentation | `<live URL>/swagger-ui.html` (spec: `<live URL>/v3/api-docs`, saved in `docs/openapi.json`) | [ ] |
| 4 | Loom video | Loom share link set to "Anyone with the link can view" | [ ] |

## Before you submit
- [ ] `mvn test` is green locally and the GitHub Actions run is green.
- [ ] Open the live URL in a private window: `/actuator/health` shows `UP`, `/swagger-ui.html` loads, `GET /api/v1/dispatch/plan` returns a plan.
- [ ] In Swagger "Try it out" works (the first call after idle can take about a minute).
- [ ] Postman verification collection: set `baseUrl` to the live URL and run it. All tests pass.
- [ ] `docs/openapi.json` was downloaded from the live `/v3/api-docs` and committed.
- [ ] README top table has the real live, Swagger and Loom links.
- [ ] Loom link opens in a private window without logging in.

## Loom script (about 5 to 6 minutes)

| Time | Show | Say |
|---|---|---|
| 0:00 | Title or README top | Who you are, the problem: allocate delivery orders to vehicles by priority and capacity while minimizing distance. |
| 0:30 | Architecture diagram in README | Layers: controller, service, strategy, repository. Strategy and Factory patterns, Haversine, Flyway, H2 or PostgreSQL. |
| 1:15 | Live Swagger UI | The deployed URL. Point out the API groups: orders, fleet, dispatch plan, plan history. |
| 1:45 | `POST /orders`, `POST /vehicles` | The assignment's required inputs. Data is already seeded, so show a small custom example, or re-submit to show upsert. |
| 2:30 | `GET /plan` | Walk through one vehicle: load below capacity, distance, route order. Show `unassignedOrders` with reasons. Explain: 645 total weight vs 500 capacity, so only LOW orders drop. |
| 3:30 | `POST /plans` then `/plans/{id}/summary` | Saved snapshot, fleet utilization, assignment rate. Try `?strategy=priority-only` and compare distance with `greedy`. |
| 4:15 | Error handling | Send a bad latitude or negative weight, show the 400 with field errors. Show a 404 and the `correlationId`. |
| 4:45 | Code or terminal | `mvn test` result, JaCoCo report or the green GitHub Actions run. |
| 5:15 | Wrap-up | Algorithm trade-off: greedy plus 2-opt is a fast heuristic, not optimal. Next steps: OR-Tools strategy, security, caching. |

Tips: warm the live app up two minutes before recording, close unrelated tabs, record at 1080p, keep it under 7 minutes.
