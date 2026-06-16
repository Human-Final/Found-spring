// src/main/resources/static/js/map.js
function initMap(placeName) {
    var mapContainer = document.getElementById('map');
    var errorBox = document.getElementById('map-error');
    
    if (!placeName || placeName.trim() === "") {
        hideMap(mapContainer, errorBox);
        return;
    }

    var map = new kakao.maps.Map(mapContainer, { center: new kakao.maps.LatLng(37.5665, 126.9780), level: 4 });
    var ps = new kakao.maps.services.Places();

    ps.keywordSearch(placeName, function(data, status) {
        if (status === kakao.maps.services.Status.OK) {
            var coords = new kakao.maps.LatLng(data[0].y, data[0].x);
            new kakao.maps.Marker({ map: map, position: coords });
            map.setCenter(coords);
            map.setLevel(3);
        } else {
            hideMap(mapContainer, errorBox);
        }
    });
}

function hideMap(container, error) {
    container.style.display = 'none';
    error.style.display = 'flex';
}