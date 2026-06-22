document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.player-token').forEach(function(card, index){
        card.style.animationDelay = (index * 0.04) + 's';
    });
});
