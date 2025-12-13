<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Write a Review</title>
    <style>
        body { background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, sans-serif; }
        .container { max-width: 600px; margin: 40px auto; background: white; padding: 40px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        h1 { margin: 0 0 10px 0; font-size: 28px; }
        .subtitle { color: #666; font-size: 16px; margin-bottom: 30px; }
        .form-group { margin-bottom: 25px; }
        label { display: block; font-weight: 600; margin-bottom: 8px; font-size: 14px; }
        input, textarea { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; font-family: inherit; box-sizing: border-box; }
        textarea { min-height: 150px; }
        .char-count { font-size: 12px; color: #999; margin-top: 5px; }
        .buttons { display: flex; gap: 12px; margin-top: 30px; }
        button { flex: 1; padding: 12px; border: none; border-radius: 6px; font-size: 16px; font-weight: 600; cursor: pointer; }
        .btn-primary { background-color: #e63946; color: white; }
        .btn-primary:hover { background-color: #d62828; }
        .btn-secondary { background-color: #f0f0f0; color: #333; }
        .btn-secondary:hover { background-color: #e0e0e0; }
        .error { color: #d62828; margin-top: 10px; display: none; font-weight: 600; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Write a Review</h1>
        <p class="subtitle"><%=request.getParameter("locationName") != null ? request.getParameter("locationName") : "Restaurant"%></p>

        <form id="reviewForm">
            <input type="hidden" id="locationId" value="<%=request.getParameter("locationId")%>">

            <div class="form-group">
                <label for="rating">Rating (1-5) *</label>
                <input type="number" id="rating" min="1" max="5" step="0.5" placeholder="e.g., 4.5" required>
            </div>

            <div class="form-group">
                <label for="title">Review Title *</label>
                <input type="text" id="title" placeholder="Sum it up..." maxlength="100" required>
            </div>

            <div class="form-group">
                <label for="body">Your Review *</label>
                <textarea id="body" placeholder="Share your experience (min 50 chars)..." required></textarea>
                <div class="char-count"><span id="charCount">0</span> / 1000</div>
            </div>

            <div class="error" id="error"></div>

            <div class="buttons">
                <button type="button" class="btn-secondary" onclick="history.back()">Cancel</button>
                <button type="submit" class="btn-primary">Submit</button>
            </div>
        </form>
    </div>

    <script>
        document.getElementById('body').addEventListener('input', (e) => {
            document.getElementById('charCount').textContent = e.target.value.length;
        });

        document.getElementById('reviewForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const locationId = document.getElementById('locationId').value;
            const rating = parseFloat(document.getElementById('rating').value);
            const title = document.getElementById('title').value;
            const body = document.getElementById('body').value;
            
            if (!locationId || !rating || rating < 1 || rating > 5 || !title || body.length < 50) {
                document.getElementById('error').textContent = 'Please fill all fields. Review must be at least 50 characters.';
                document.getElementById('error').style.display = 'block';
                return;
            }
            
            try {
                const fullUrl = '/foodlocator/api/reviews';
                console.log('Submitting to:', fullUrl);
                
                const resp = await fetch(fullUrl, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({
                        locationID: parseInt(locationId),
                        rating: rating,
                        title: title,
                        body: body,
                        tags: []
                    })
                });
                
                console.log('Response status:', resp.status);
                const data = await resp.json();
                console.log('Response data:', data);
                
                if (data.success) {
                    alert('Review submitted!');
                    window.location.href = '/foodlocator/';
                } else {
                    document.getElementById('error').textContent = 'Error: ' + (data.error || 'Failed to submit');
                    document.getElementById('error').style.display = 'block';
                }
            } catch (e) {
                console.error('Error:', e);
                document.getElementById('error').textContent = 'Error: ' + e.message;
                document.getElementById('error').style.display = 'block';
            }
        });
    </script>
</body>
</html>
