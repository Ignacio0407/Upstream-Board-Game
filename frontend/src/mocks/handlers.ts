import { http, HttpResponse } from 'msw';

export const handlers = [
    // Auth
    http.post('*/api/v1/auth/signin', async ({ request }) => {
        const body = await request.json() as any;
        if (body.username === 'wronguser') {
            return new HttpResponse(null, { status: 401 });
        }
        return HttpResponse.json({
            token: 'mock-jwt',
            username: body.username || 'admin',
            user: { id: 1, username: 'admin', authority: { authority: 'ADMIN' } }
        });
    }),

    http.post('*/api/v1/auth/signup', async ({ request }) => {
        const body = await request.json() as any;
        if (body.username === 'existinguser') {
            return HttpResponse.json({ message: "User already exists" }, { status: 400 });
        }
        return HttpResponse.json({ id: 3, ...body }, { status: 201 });
    }),

    // Achievements
    http.get('*/api/v1/achievements', () => {
        return HttpResponse.json([
            { id: 1, name: "First Win", description: "Win once", badgeImage: "/img/badge1.png", threshold: 1, metric: "wins" },
            { id: 2, name: "No Badge", description: "No image", badgeImage: null, threshold: 5, metric: "games" },
        ]);
    }),

    // Users
    http.get('*/api/v1/users/:id', ({ params }) => {
        return HttpResponse.json({
            id: params.id,
            username: "testuser",
            victories: 3,
            playedgames: 10,
            totalpoints: 50
        });
    }),

    http.get('*/api/v1/users/:id/achievements', () => {
        return HttpResponse.json([{ id: 1 }]);
    }),

    http.get('*/api/v1/users/listing', () => {
        return HttpResponse.json([
            { id: 1, name: "Alice", authority: "USER" },
            { id: 2, name: "Bob", authority: "ADMIN" }
        ]);
    }),

    http.patch('*/api/v1/users/listing/:id', ({ params }) => {
        return HttpResponse.json({ id: params.id, name: "Alice", authority: "ADMIN" });
    }),

    http.get('*/api/v1/auth/validate', ({ request }) => {
        const url = new URL(request.url);
        const token = url.searchParams.get('token');
        if (token === 'valid-jwt') {
            return HttpResponse.json(true);
        }
        return HttpResponse.json(false);
    }),

    // Default handlers for common patterns
    http.get('*/api/v1/:resource', () => {
        return HttpResponse.json([]);
    }),

    http.get('*/api/v1/:resource/:id', () => {
        return HttpResponse.json({});
    }),

    http.post('*/api/v1/:resource', () => {
        return HttpResponse.json({}, { status: 201 });
    }),

    http.put('*/api/v1/:resource/:id', () => {
        return HttpResponse.json({});
    }),

    http.patch('*/api/v1/:resource/:id', () => {
        return HttpResponse.json({});
    }),

    http.delete('*/api/v1/:resource/:id', () => {
        return HttpResponse.json({ message: 'Deleted' });
    }),
];
